package com.androidserver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBase = Color(0xFF0B0F19)
val CardDark = Color(0xFF161E2E)
val PrimaryIndigo = Color(0xFF6366F1)
val SuccessEmerald = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRose = Color(0xFFEF4444)
val TextLight = Color(0xFFF3F4F6)
val TextMuted = Color(0xFF9CA3AF)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SuccessEmerald,
    background = DarkBase,
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight
)

@Composable
fun AndroidServerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
