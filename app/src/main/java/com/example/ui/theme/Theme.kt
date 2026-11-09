package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.ui.designsystem.FlowShapes
import com.example.ui.designsystem.FlowSpacing
import com.example.ui.designsystem.FlowThemeState
import com.example.ui.designsystem.FlowTypography
import com.example.ui.designsystem.LocalFlowColors
import com.example.ui.designsystem.LocalFlowShapes
import com.example.ui.designsystem.LocalFlowSpacing
import com.example.ui.designsystem.LocalFlowThemeState
import com.example.ui.designsystem.LocalFlowTypography
import com.example.ui.designsystem.ThemeMode

@Composable
fun MyApplicationTheme(
    themeState: FlowThemeState = remember { FlowThemeState() },
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemInDark
    }

    val flowColors = remember(themeState.accentTheme, isDark) {
        createFlowColors(themeState.accentTheme, isDark)
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = flowColors.accent,
            onPrimary = flowColors.onAccent,
            primaryContainer = flowColors.accentContainer,
            onPrimaryContainer = flowColors.onAccentContainer,
            secondary = flowColors.accent,
            onSecondary = flowColors.onAccent,
            background = flowColors.background,
            onBackground = flowColors.textPrimary,
            surface = flowColors.surface,
            onSurface = flowColors.textPrimary,
            surfaceVariant = flowColors.surfaceElevated,
            onSurfaceVariant = flowColors.textSecondary,
            outline = flowColors.border,
            error = flowColors.error,
            onError = flowColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = flowColors.accent,
            onPrimary = flowColors.onAccent,
            primaryContainer = flowColors.accentContainer,
            onPrimaryContainer = flowColors.onAccentContainer,
            secondary = flowColors.accent,
            onSecondary = flowColors.onAccent,
            background = flowColors.background,
            onBackground = flowColors.textPrimary,
            surface = flowColors.surface,
            onSurface = flowColors.textPrimary,
            surfaceVariant = flowColors.surfaceElevated,
            onSurfaceVariant = flowColors.textSecondary,
            outline = flowColors.border,
            error = flowColors.error,
            onError = flowColors.textPrimary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalFlowColors provides flowColors,
        LocalFlowThemeState provides themeState,
        LocalFlowSpacing provides FlowSpacing(),
        LocalFlowShapes provides FlowShapes(),
        LocalFlowTypography provides FlowTypography()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
