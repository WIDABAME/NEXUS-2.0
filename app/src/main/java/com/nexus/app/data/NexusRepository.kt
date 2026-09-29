package com.nexus.app.data

import kotlinx.coroutines.flow.Flow

class NexusRepository(private val dao: NexusDao) {

    val notes: Flow<List<Note>> = dao.getNotes()
    val links: Flow<List<NoteLink>> = dao.getLinks()

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

    suspend fun autoLinkAllNotes(): Int {
        val allNotes = dao.getAllNotesList()
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
        return totalLinksCreated
    }
}
