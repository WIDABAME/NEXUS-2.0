package com.nexus.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color palette definitions
val NexusBackground = Color(0xFF0B1019)
val NexusSurface = Color(0xFF121824)
val NexusEmerald = Color(0xFF0B8272)
val NexusEmeraldDark = Color(0xFF055149)
val NexusAccent = Color(0xFF22D3BB)
val NexusMintLight = Color(0xFF7DF2DD)
val NexusTextPrimary = Color(0xFFF0F4F8)
val NexusTextSecondary = Color(0xFF6E7C91)

private val NexusColorScheme = darkColorScheme(
    primary = NexusAccent,
    onPrimary = Color(0xFF0B1019),
    primaryContainer = NexusEmerald,
    onPrimaryContainer = Color.White,
    secondary = NexusMintLight,
    onSecondary = Color(0xFF0B1019),
    background = NexusBackground,
    onBackground = NexusTextPrimary,
    surface = NexusSurface,
    onSurface = NexusTextPrimary,
    surfaceVariant = Color(0xFF161F2E),
    onSurfaceVariant = NexusTextSecondary
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusColorScheme,
        content = content
    )
}
