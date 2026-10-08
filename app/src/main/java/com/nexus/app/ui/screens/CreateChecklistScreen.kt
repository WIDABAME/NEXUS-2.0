package com.nexus.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nexus.app.ui.theme.NexusAccent
import com.nexus.app.ui.theme.NexusBackground
import com.nexus.app.ui.theme.NexusSurface
import com.nexus.app.ui.theme.NexusTextPrimary
import com.nexus.app.ui.theme.NexusTextSecondary
import com.nexus.app.viewmodel.NexusViewModel

data class DraftChecklistItem(
    val text: String = "",
    val isChecked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChecklistScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    val draftItems = remember { mutableStateListOf(DraftChecklistItem(), DraftChecklistItem()) }

    Scaffold(
        containerColor = NexusBackground,
        topBar = {
            TopAppBar(
                title = { Text("Nueva lista", fontWeight = FontWeight.Bold) },
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
                            val validTitle = if (title.isBlank()) "Sin título" else title
                            val validItems = draftItems.filter { it.text.isNotBlank() }
                            if (title.isNotBlank() || validItems.isNotEmpty()) {
                                viewModel.createChecklist(
                                    title = validTitle,
                                    items = validItems.map { it.text to it.isChecked }
                                ) {
                                    onCreated()
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Título de la lista", color = NexusTextSecondary) },
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

            Spacer(Modifier.height(20.dp))

            Text(
                "Ítems de la lista",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(draftItems) { index, draftItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = draftItem.isChecked,
                            onCheckedChange = { checked ->
                                draftItems[index] = draftItem.copy(isChecked = checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NexusAccent,
                                checkmarkColor = NexusBackground,
                                uncheckedColor = NexusTextSecondary
                            )
                        )

                        Spacer(Modifier.width(4.dp))

                        OutlinedTextField(
                            value = draftItem.text,
                            onValueChange = { newText ->
                                draftItems[index] = draftItem.copy(text = newText)
                            },
                            placeholder = { Text("Ítem ${index + 1}", color = NexusTextSecondary) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = NexusTextPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (index == draftItems.lastIndex) {
                                        draftItems.add(DraftChecklistItem())
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexusAccent,
                                unfocusedBorderColor = Color(0xFF1C2638),
                                focusedContainerColor = NexusSurface,
                                unfocusedContainerColor = NexusSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (draftItems.size > 1) {
                                    draftItems.removeAt(index)
                                } else {
                                    draftItems[0] = DraftChecklistItem()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar ítem", tint = NexusTextSecondary)
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { draftItems.add(DraftChecklistItem()) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar otro ítem")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
