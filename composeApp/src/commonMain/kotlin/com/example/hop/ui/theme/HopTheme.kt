package com.example.hop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Hop is a dark-themed app (Spotify-inspired).
// HopColors.surface / surfaceElevated are the primary canvas colours.
// HopColors.background (white) is reserved for light-surface exceptions
// (e.g. modals, onboarding) — use it directly via HopColors when needed.
private val HopColorScheme = darkColorScheme(
    primary           = HopColors.primaryLime,
    onPrimary         = Color(0xFF1A1A1A),       // dark text on lime — meets 4.5:1
    secondary         = HopColors.primaryGreen,
    onSecondary       = Color(0xFFFFFFFF),
    background        = HopColors.surface,        // main canvas = dark
    onBackground      = HopColors.textPrimary,
    surface           = HopColors.surface,
    onSurface         = HopColors.textPrimary,
    surfaceVariant    = HopColors.surfaceElevated,
    onSurfaceVariant  = HopColors.textSecondary,
    error             = HopColors.error,
    onError           = Color(0xFFFFFFFF),
)

@Composable
fun HopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HopColorScheme,
        typography  = HopTypography,
        content     = content,
    )
}
