package com.nexus.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.data.Note
import com.nexus.app.ui.theme.NexusTextSecondary
import com.nexus.app.viewmodel.NexusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    var note by remember { mutableStateOf<Note?>(null) }
    val allNotes by viewModel.allNotes.collectAsState()
    val links by viewModel.links.collectAsState()
    var showLinkPicker by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        note = viewModel.getNote(noteId)
    }

    val current = note
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var title by remember(current.id) { mutableStateOf(current.title) }
    var content by remember(current.id) { mutableStateOf(current.content) }

    val relatedLinks = links.filter { it.fromNoteId == current.id || it.toNoteId == current.id }
    val linkedNotes = relatedLinks.mapNotNull { link ->
        val otherId = if (link.fromNoteId == current.id) link.toNoteId else link.fromNoteId
        allNotes.find { it.id == otherId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nota") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.nexus.app.ui.theme.NexusBackground,
                    titleContentColor = com.nexus.app.ui.theme.NexusTextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateNote(current.copy(title = title, content = content))
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = com.nexus.app.ui.theme.NexusTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showLinkPicker = true }) {
                        Icon(Icons.Default.Link, contentDescription = "Enlazar nota", tint = com.nexus.app.ui.theme.NexusAccent)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(Modifier.height(12.dp))
            if (linkedNotes.isNotEmpty()) {
                Text("Enlazada con", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LazyColumn {
                    items(linkedNotes, key = { it.id }) { linked ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                "• ${linked.title}",
                                color = NexusTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    viewModel.deleteLinkBetween(current.id, linked.id)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Desenlazar", color = com.nexus.app.ui.theme.NexusAccent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLinkPicker) {
        AlertDialog(
            onDismissRequest = { showLinkPicker = false },
            title = { Text("Enlazar con...") },
            text = {
                LazyColumn {
                    items(allNotes.filter { it.id != current.id }) { other ->
                        TextButton(onClick = {
                            viewModel.addLink(current.id, other.id)
                            showLinkPicker = false
                        }) {
                            Text(other.title)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkPicker = false }) { Text("Cerrar") }
            }
        )
    }
}
