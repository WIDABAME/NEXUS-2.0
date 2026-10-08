package com.nexus.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.app.data.Note
import com.nexus.app.data.NoteLink
import com.nexus.app.ui.theme.NexusAccent
import com.nexus.app.ui.theme.NexusTextSecondary
import com.nexus.app.viewmodel.NexusViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: NexusViewModel, onOpenNote: (Long) -> Unit) {
    val notes by viewModel.allNotes.collectAsState()
    val links by viewModel.links.collectAsState()

    var selectedNoteId by remember { mutableStateOf<Long?>(null) }
    val currentOnOpenNote by rememberUpdatedState(onOpenNote)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(notes) {
        if ((selectedNoteId == null) || notes.none { it.id == selectedNoteId }) {
            selectedNoteId = notes.firstOrNull()?.id
        }
    }

    val activeNote = notes.find { it.id == selectedNoteId }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = com.nexus.app.ui.theme.NexusBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Grafo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${notes.size} notas · ${links.size} enlaces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.autoLinkAllNotes { createdCount ->
                            coroutineScope.launch {
                                val message = if (createdCount > 0) {
                                    "¡Se crearon $createdCount nuevos enlaces automáticos!"
                                } else {
                                    "Todas las notas relacionadas ya están conectadas."
                                }
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = com.nexus.app.ui.theme.NexusEmerald,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Auto-conectar", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Crea notas para ver el grafo", color = NexusTextSecondary)
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val density = LocalDensity.current
                    val widthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
                    val heightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }

                    val positions = remember(notes, widthPx, heightPx) {
                        computePixelLayout(notes, widthPx, heightPx)
                    }

                    //Dibujar líneas de conexión en Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = 2.5.dp.toPx()
                        links.forEach { link: NoteLink ->
                            val from = positions[link.fromNoteId]
                            val to = positions[link.toNoteId]
                            if ((from != null) && (to != null)) {
                                drawLine(
                                    color = Color(0xFF0B8272),
                                    start = from,
                                    end = to,
                                    strokeWidth = strokeWidthPx
                                )
                            }
                        }
                    }

                    // 2. Renderizar nodos clicables sobre el Canvas
                    val nodeRadiusPx = with(density) { 26.dp.toPx() }

                    notes.forEach { note ->
                        val pos = positions[note.id] ?: return@forEach
                        val isSelected = note.id == activeNote?.id

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val x = (pos.x - placeable.width / 2f).toInt()
                                    val y = (pos.y - nodeRadiusPx).toInt()
                                    layout(placeable.width, placeable.height) {
                                        placeable.placeWithLayer(x, y)
                                    }
                                }
                                .clickable {
                                    if (selectedNoteId == note.id) {
                                        currentOnOpenNote(note.id)
                                    } else {
                                        selectedNoteId = note.id
                                    }
                                }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) NexusAccent else com.nexus.app.ui.theme.NexusSurface,
                                border = BorderStroke(2.5.dp, NexusAccent),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = note.title.take(2).uppercase(),
                                        color = if (isSelected) com.nexus.app.ui.theme.NexusBackground else Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = com.nexus.app.ui.theme.NexusSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF1C2638))
                            ) {
                                Text(
                                    text = note.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                activeNote?.let { note ->
                    Card(
                        onClick = { currentOnOpenNote(note.id) },
                        colors = CardDefaults.cardColors(containerColor = com.nexus.app.ui.theme.NexusSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexusTextSecondary,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { currentOnOpenNote(note.id) }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir nota")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computePixelLayout(notes: List<Note>, widthPx: Float, heightPx: Float): Map<Long, Offset> {
    val centerX = widthPx / 2f
    val centerY = heightPx * 0.45f
    val radius = minOf(widthPx, heightPx) * 0.35f
    val positions = mutableMapOf<Long, Offset>()
    val n = notes.size.coerceAtLeast(1)
    if (n == 1) {
        positions[notes[0].id] = Offset(centerX, centerY)
        return positions
    }
    notes.forEachIndexed { index, note ->
        val angle = (2 * Math.PI * index / n)
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        positions[note.id] = Offset(x, y)
    }
    return positions
}
