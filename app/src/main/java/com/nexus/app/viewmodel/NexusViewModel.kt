package com.nexus.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.app.data.NexusDatabase
import com.nexus.app.data.NexusRepository
import com.nexus.app.data.Note
import com.nexus.app.data.NoteLink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: NexusRepository = NexusRepository(
        NexusDatabase.getInstance(application, viewModelScope).nexusDao()
    )

    init {
        viewModelScope.launch {
            repo.autoLinkAllNotes()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val allNotes: StateFlow<List<Note>> = repo.notes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val links: StateFlow<List<NoteLink>> = repo.links.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val filteredNotes: StateFlow<List<Note>> = combine(allNotes, _searchQuery) { notes, query ->
        if (query.isBlank()) notes
        else notes.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun addNote(title: String, content: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.addNote(title, content)
            onCreated(id)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { repo.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repo.deleteNote(note) }
    }

    fun addLink(fromId: Long, toId: Long) {
        viewModelScope.launch { repo.addLink(fromId, toId) }
    }

    fun deleteLink(linkId: Long) {
        viewModelScope.launch { repo.deleteLink(linkId) }
    }

    fun deleteLinkBetween(id1: Long, id2: Long) {
        viewModelScope.launch { repo.deleteLinkBetween(id1, id2) }
    }

    fun autoLinkAllNotes(onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val created = repo.autoLinkAllNotes()
            onResult(created)
        }
    }

    suspend fun getNote(id: Long): Note? = repo.getNote(id)
}
