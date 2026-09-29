package com.nexus.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_links")
data class NoteLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromNoteId: Long,
    val toNoteId: Long
)
