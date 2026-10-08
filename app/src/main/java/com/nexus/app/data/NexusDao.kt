package com.nexus.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NexusDao {

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note_links")
    fun getLinks(): Flow<List<NoteLink>>

    @Insert
    suspend fun insertLink(link: NoteLink): Long

    @Query("DELETE FROM note_links WHERE id = :linkId")
    suspend fun deleteLinkById(linkId: Long)

    @Query("DELETE FROM note_links WHERE fromNoteId = :noteId OR toNoteId = :noteId")
    suspend fun deleteLinksForNote(noteId: Long)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Int

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesList(): List<Note>

    @Query("SELECT COUNT(*) > 0 FROM note_links WHERE (fromNoteId = :fromId AND toNoteId = :toId) OR (fromNoteId = :toId AND toNoteId = :fromId)")
    suspend fun linkExists(fromId: Long, toId: Long): Boolean

    @Query("DELETE FROM note_links WHERE (fromNoteId = :id1 AND toNoteId = :id2) OR (fromNoteId = :id2 AND toNoteId = :id1)")
    suspend fun deleteLinkBetween(id1: Long, id2: Long)

    // Checklists queries & mutations
    @Transaction
    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    fun getChecklistsWithItems(): Flow<List<ChecklistWithItems>>

    @Transaction
    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getChecklistWithItemsById(id: Long): ChecklistWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: Checklist): Long

    @Update
    suspend fun updateChecklist(checklist: Checklist)

    @Delete
    suspend fun deleteChecklist(checklist: Checklist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItem): Long

    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItem)

    @Query("DELETE FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun deleteItemsForChecklist(checklistId: Long)
}
