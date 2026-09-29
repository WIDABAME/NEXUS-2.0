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
}
