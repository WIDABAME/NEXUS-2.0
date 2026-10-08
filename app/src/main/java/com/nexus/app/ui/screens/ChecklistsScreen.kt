package com.nexus.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nexus.app.data.Checklist
import com.nexus.app.data.ChecklistItem
import com.nexus.app.data.ChecklistWithItems
import com.nexus.app.ui.theme.NexusAccent
import com.nexus.app.ui.theme.NexusSurface
import com.nexus.app.ui.theme.NexusTextSecondary
import com.nexus.app.viewmodel.NexusViewModel

@Composable
fun ChecklistsScreen(
    viewModel: NexusViewModel,
    onCreateChecklist: () -> Unit
) {
    val checklists by viewModel.checklists.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateChecklist,
                containerColor = NexusAccent,
                contentColor = com.nexus.app.ui.theme.NexusBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva lista")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Header section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Listas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${checklists.size} listas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (checklists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes listas creadas.\n¡Haz clic en + para crear una lista!",
                        color = NexusTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(checklists, key = { it.checklist.id }) { checklistWithItems ->
                        ChecklistCard(
                            checklistWithItems = checklistWithItems,
                            onToggleItem = { item -> viewModel.toggleChecklistItem(item) },
                            onAddItem = { checklistId, text -> viewModel.addChecklistItem(checklistId, text) },
                            onDeleteItem = { item -> viewModel.deleteChecklistItem(item) },
                            onDeleteChecklist = { checklist -> viewModel.deleteChecklist(checklist) },
                            onRenameChecklist = { checklist, newTitle -> viewModel.updateChecklistTitle(checklist, newTitle) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ChecklistCard(
    checklistWithItems: ChecklistWithItems,
    onToggleItem: (ChecklistItem) -> Unit,
    onAddItem: (Long, String) -> Unit,
    onDeleteItem: (ChecklistItem) -> Unit,
    onDeleteChecklist: (Checklist) -> Unit,
    onRenameChecklist: (Checklist, String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var newItemText by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }

    val completedCount = checklistWithItems.items.count { it.isChecked }
    val totalCount = checklistWithItems.items.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Controles de título y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = NexusAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = checklistWithItems.checklist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Renombrar", tint = NexusTextSecondary)
                    }
                    IconButton(onClick = { onDeleteChecklist(checklistWithItems.checklist) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = NexusTextSecondary)
                    }
                }
            }

            // Barra de progreso y contador
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    color = NexusAccent,
                    trackColor = Color(0xFF161F2E)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = NexusTextSecondary
                )
            }

            // Contenido expandible
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    checklistWithItems.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleItem(item) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onToggleItem(item) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NexusAccent,
                                    checkmarkColor = com.nexus.app.ui.theme.NexusBackground,
                                    uncheckedColor = NexusTextSecondary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (item.isChecked) NexusTextSecondary else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteItem(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar item",
                                    tint = NexusTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Campo para agregar un nuevo elemento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemText,
                            onValueChange = { newItemText = it },
                            placeholder = { Text("Agregar ítem...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newItemText.isNotBlank()) {
                                    onAddItem(checklistWithItems.checklist.id, newItemText)
                                    newItemText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar ítem", tint = NexusAccent)
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var renameText by remember { mutableStateOf(checklistWithItems.checklist.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Renombrar lista") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Título de la lista") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameChecklist(checklistWithItems.checklist, renameText)
                            showRenameDialog = false
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
