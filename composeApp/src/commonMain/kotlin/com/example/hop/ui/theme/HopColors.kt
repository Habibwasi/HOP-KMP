package com.example.hop.ui.theme

import androidx.compose.ui.graphics.Color

object HopColors {
    val primaryLime      = Color(0xFFC8F135)
    val primaryGreen     = Color(0xFF1DB954)
    val background       = Color(0xFFFFFFFF)
    val surface          = Color(0xFF1A1A1A)
    val surfaceElevated  = Color(0xFF242424)
    val textPrimary      = Color(0xFFFFFFFF)
    val textSecondary    = Color(0xFFB3B3B3)
    val success          = Color(0xFF22C55E)
    val warning          = Color(0xFFFBBF24)
    val error            = Color(0xFFEF4444)

    // ── Auth screens (white background) ──────────────────────────────────────
    // authTextPrimary:   near-black, contrast ratio ~18:1 on white (WCAG AAA)
    // authTextSecondary: medium slate-gray, ~7.1:1 on white (WCAG AA Large)
    // authAccent:        dark green, ~5.3:1 on white (WCAG AA)
    // authInputSurface:  off-white for input fields on white canvas
    // authInputBorder:   subtle cool-gray border visible on white
    val authTextPrimary    = Color(0xFF0D0D0D)
    val authTextSecondary  = Color(0xFF5F6368)
    val authAccent         = Color(0xFF167A30)
    val authInputSurface   = Color(0xFFF1F3F4)
    val authInputBorder    = Color(0xFFD1D5DB)
}
