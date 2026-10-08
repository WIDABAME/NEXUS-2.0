package com.nexus.app.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class NexusRepository(private val dao: NexusDao) {

    val notes: Flow<List<Note>> = dao.getNotes()
    val links: Flow<List<NoteLink>> = dao.getLinks()
    val checklists: Flow<List<ChecklistWithItems>> = dao.getChecklistsWithItems()

    suspend fun createChecklist(title: String, items: List<Pair<String, Boolean>>): Long {
        val checklistId = dao.insertChecklist(Checklist(title = title))
        items.forEachIndexed { index, (text, isChecked) ->
            if (text.isNotBlank()) {
                dao.insertChecklistItem(
                    ChecklistItem(
                        checklistId = checklistId,
                        text = text,
                        isChecked = isChecked,
                        position = index
                    )
                )
            }
        }
        return checklistId
    }

    suspend fun addChecklistItem(checklistId: Long, text: String) {
        if (text.isNotBlank()) {
            dao.insertChecklistItem(ChecklistItem(checklistId = checklistId, text = text))
        }
    }

    suspend fun toggleChecklistItem(item: ChecklistItem) {
        dao.updateChecklistItem(item.copy(isChecked = !item.isChecked))
    }

    suspend fun deleteChecklistItem(item: ChecklistItem) {
        dao.deleteChecklistItem(item)
    }

    suspend fun deleteChecklist(checklist: Checklist) {
        dao.deleteChecklist(checklist)
    }

    suspend fun updateChecklistTitle(checklist: Checklist, newTitle: String) {
        dao.updateChecklist(checklist.copy(title = newTitle))
    }

    suspend fun addNote(title: String, content: String): Long {
        val newId = dao.insertNote(Note(title = title, content = content))
        val newNote = dao.getNoteById(newId)
        if (newNote != null) {
            autoLinkNote(newNote)
        }
        return newId
    }

    suspend fun updateNote(note: Note) {
        dao.updateNote(note)
        // Invalida el embedding viejo de la nota en la caché para forzar recálculo
        AutoLinker.invalidateCacheForNote(note.id)
        // Elimina los enlaces anteriores de la nota
        dao.deleteLinksForNote(note.id)
        // Vuelve a analizar e interconectar semánticamente con las demás notas
        autoLinkNote(note)
    }

    suspend fun deleteNote(note: Note) {
        dao.deleteLinksForNote(note.id)
        dao.deleteNote(note)
    }

    suspend fun getNote(id: Long): Note? = dao.getNoteById(id)

    suspend fun addLink(fromId: Long, toId: Long) {
        if (fromId != toId && !dao.linkExists(fromId, toId)) {
            dao.insertLink(NoteLink(fromNoteId = fromId, toNoteId = toId))
        }
    }

    suspend fun deleteLink(linkId: Long) = dao.deleteLinkById(linkId)

    suspend fun deleteLinkBetween(id1: Long, id2: Long) = dao.deleteLinkBetween(id1, id2)

    suspend fun autoLinkNote(targetNote: Note): Int {
        val allNotes = dao.getAllNotesList()
        var linksCreated = 0
        for (otherNote in allNotes) {
            if (otherNote.id != targetNote.id) {
                if (AutoLinker.areNotesRelated(targetNote, otherNote)) {
                    if (!dao.linkExists(targetNote.id, otherNote.id)) {
                        dao.insertLink(NoteLink(fromNoteId = targetNote.id, toNoteId = otherNote.id))
                        linksCreated++
                    }
                }
            }
        }
        return linksCreated
    }

    suspend fun autoLinkAllNotes(): Int = coroutineScope {
        val allNotes = dao.getAllNotesList()
        if (allNotes.size < 2) return@coroutineScope 0

        // Pre-obtener los embeddings en paralelo para optimizar las llamadas a Gemini
        allNotes.map { note ->
            async { AutoLinker.getEmbeddingForNote(note) }
        }.awaitAll()

        var totalLinksCreated = 0
        for (i in allNotes.indices) {
            for (j in i + 1 until allNotes.size) {
                val noteA = allNotes[i]
                val noteB = allNotes[j]
                if (AutoLinker.areNotesRelated(noteA, noteB)) {
                    if (!dao.linkExists(noteA.id, noteB.id)) {
                        dao.insertLink(NoteLink(fromNoteId = noteA.id, toNoteId = noteB.id))
                        totalLinksCreated++
                    }
                }
            }
        }
        totalLinksCreated
    }
}
