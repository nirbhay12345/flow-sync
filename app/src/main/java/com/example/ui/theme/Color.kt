package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.ui.designsystem.AccentTheme
import com.example.ui.designsystem.FlowColors

// Legacy theme compatibility aliases
val FlowCyan = Color(0xFF7FA88B) // Refined Sage
val FlowCyanDim = Color(0xFF5E846A)
val FlowViolet = Color(0xFF9D84B7) // Refined Dusk Lavender
val FlowVioletDim = Color(0xFF786290)
val FlowCoral = Color(0xFFE06C75)
val FlowOrange = Color(0xFFE07A5F) // Warm Terracotta
val FlowGreen = Color(0xFF7FA88B)

val DarkBackground = Color(0xFF0C0E14)
val DarkSurface = Color(0xFF141720)
val DarkSurfaceVariant = Color(0xFF1B1F2C)
val DarkBorder = Color(0xFF272C3D)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val PriorityHigh = Color(0xFFE06C75)
val PriorityMedium = Color(0xFFE5A93C)
val PriorityLow = Color(0xFF5B86E5)

/**
 * Creates a complete Minimalist FlowColors palette matching the user's selected accent and mode.
 */
fun createFlowColors(accentTheme: AccentTheme, isDark: Boolean): FlowColors {
    return if (isDark) {
        FlowColors(
            background = Color(0xFF0C0E14),
            surface = Color(0xFF141720),
            surfaceElevated = Color(0xFF1B1F2C),
            surfaceSubtle = Color(0xFF222636),
            border = Color(0xFF272C3D),
            borderSubtle = Color(0xFF1B1E2B),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            accent = accentTheme.primaryColor,
            onAccent = if (accentTheme == AccentTheme.SLATE) Color(0xFF0F172A) else Color(0xFF0C1017),
            accentContainer = accentTheme.primaryColor.copy(alpha = 0.16f),
            onAccentContainer = accentTheme.primaryColor,
            priorityHigh = Color(0xFFE06C75),
            priorityMedium = Color(0xFFE5A93C),
            priorityLow = Color(0xFF5B86E5),
            success = Color(0xFF7FA88B),
            error = Color(0xFFE06C75),
            isDark = true
        )
    } else {
        FlowColors(
            background = Color(0xFFF7F8FA),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFF0F2F5),
            surfaceSubtle = Color(0xFFE9ECEF),
            border = Color(0xFFE2E6EC),
            borderSubtle = Color(0xFFEEF1F5),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textMuted = Color(0xFF94A3B8),
            accent = if (accentTheme == AccentTheme.SLATE) Color(0xFF1E293B) else accentTheme.primaryColor,
            onAccent = Color.White,
            accentContainer = accentTheme.primaryColor.copy(alpha = 0.12f),
            onAccentContainer = if (accentTheme == AccentTheme.SLATE) Color(0xFF0F172A) else accentTheme.primaryColor,
            priorityHigh = Color(0xFFDC2626),
            priorityMedium = Color(0xFFD97706),
            priorityLow = Color(0xFF2563EB),
            success = Color(0xFF16A34A),
            error = Color(0xFFDC2626),
            isDark = false
        )
    }
}
