package com.nexus.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.ui.theme.NexusAccent
import com.nexus.app.ui.theme.NexusBackground
import com.nexus.app.ui.theme.NexusSurface
import com.nexus.app.ui.theme.NexusTextPrimary
import com.nexus.app.ui.theme.NexusTextSecondary
import com.nexus.app.viewmodel.NexusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NexusBackground,
        topBar = {
            TopAppBar(
                title = { Text("Nueva nota", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NexusBackground,
                    titleContentColor = NexusTextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = NexusTextPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                viewModel.addNote(
                                    title = if (title.isBlank()) "Sin título" else title,
                                    content = content
                                ) { newId ->
                                    onCreated(newId)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusAccent,
                            contentColor = NexusBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Título", color = NexusTextSecondary) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusAccent,
                    unfocusedBorderColor = Color(0xFF1C2638),
                    focusedContainerColor = NexusSurface,
                    unfocusedContainerColor = NexusSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Escribe el contenido de tu nota aquí...", color = NexusTextSecondary) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = NexusTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusAccent,
                    unfocusedBorderColor = Color(0xFF1C2638),
                    focusedContainerColor = NexusSurface,
                    unfocusedContainerColor = NexusSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
