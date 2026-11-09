package com.example.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Curated Minimalist Accent Themes for FlowPulse.
 * These are restrained, sophisticated tones avoiding garish neon glow.
 */
enum class AccentTheme(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val previewColor: Color
) {
    SAGE(
        title = "Nordic Sage",
        description = "Calm, organic, and grounded",
        primaryColor = Color(0xFF7FA88B),
        previewColor = Color(0xFF7FA88B)
    ),
    TERRACOTTA(
        title = "Warm Terracotta",
        description = "Earthy warmth with industrial feel",
        primaryColor = Color(0xFFE07A5F),
        previewColor = Color(0xFFE07A5F)
    ),
    SLATE(
        title = "Studio Slate",
        description = "Pure, distraction-free monochrome",
        primaryColor = Color(0xFFE2E8F0),
        previewColor = Color(0xFF94A3B8)
    ),
    COBALT(
        title = "Quiet Cobalt",
        description = "Deep Nordic focus and clarity",
        primaryColor = Color(0xFF5B86E5),
        previewColor = Color(0xFF5B86E5)
    ),
    LAVENDER(
        title = "Dusk Lavender",
        description = "Soft, contemplative twilight",
        primaryColor = Color(0xFF9D84B7),
        previewColor = Color(0xFF9D84B7)
    ),
    AMBER(
        title = "Solar Amber",
        description = "Warm honey without neon glare",
        primaryColor = Color(0xFFE5A93C),
        previewColor = Color(0xFFE5A93C)
    )
}

enum class ThemeMode(val title: String) {
    DARK("Dark Minimal"),
    LIGHT("Light Paper"),
    SYSTEM("System Default")
}

enum class TimerStyle(val title: String, val subtitle: String) {
    MINIMAL_RING("Minimal Arc", "Subtle hairline ring with arc progress"),
    ZEN_NUMERIC("Zen Typographic", "Clean tabular digits with linear baseline")
}

/**
 * Customizable preferences for FlowPulse Design System.
 */
class FlowThemeState(
    initialAccent: AccentTheme = AccentTheme.SAGE,
    initialMode: ThemeMode = ThemeMode.DARK,
    initialTimerStyle: TimerStyle = TimerStyle.MINIMAL_RING,
    initialWorkMinutes: Int = 25,
    initialShortBreak: Int = 5,
    initialLongBreak: Int = 15
) {
    var accentTheme by mutableStateOf(initialAccent)
    var themeMode by mutableStateOf(initialMode)
    var timerStyle by mutableStateOf(initialTimerStyle)
    var workMinutes by mutableIntStateOf(initialWorkMinutes)
    var shortBreakMinutes by mutableIntStateOf(initialShortBreak)
    var longBreakMinutes by mutableIntStateOf(initialLongBreak)
}

/**
 * Minimalist Semantic Color Palette
 */
data class FlowColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val priorityHigh: Color,
    val priorityMedium: Color,
    val priorityLow: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean
)

val LocalFlowColors = staticCompositionLocalOf<FlowColors> {
    error("No FlowColors provided")
}

val LocalFlowThemeState = staticCompositionLocalOf<FlowThemeState> {
    FlowThemeState()
}

/**
 * Spacing design tokens
 */
data class FlowSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 28.dp,
    val xxxl: Dp = 36.dp
)

val LocalFlowSpacing = staticCompositionLocalOf { FlowSpacing() }

/**
 * Shapes design tokens
 */
data class FlowShapes(
    val xs: Dp = 6.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 18.dp,
    val xl: Dp = 24.dp,
    val pill: Dp = 999.dp
)

val LocalFlowShapes = staticCompositionLocalOf { FlowShapes() }

/**
 * Typography design tokens
 */
data class FlowTypography(
    val display: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 40.sp
    ),
    val timerDigits: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 58.sp,
        letterSpacing = (-1.5).sp,
        lineHeight = 62.sp
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = (-0.2).sp,
        lineHeight = 28.sp
    ),
    val titleLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp,
        lineHeight = 24.sp
    ),
    val titleMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.sp,
        lineHeight = 20.sp
    ),
    val titleSmall: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        lineHeight = 18.sp
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.1.sp,
        lineHeight = 22.sp
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.1.sp,
        lineHeight = 18.sp
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 16.sp
    ),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.3.sp,
        lineHeight = 18.sp
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 14.sp
    ),
    val labelSmall: TextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        lineHeight = 12.sp
    ),
    val monoLabel: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.sp
    )
)

val LocalFlowTypography = staticCompositionLocalOf { FlowTypography() }

/**
 * Accessor object for easy access to the design system tokens.
 */
object FlowTheme {
    val colors: FlowColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowColors.current

    val spacing: FlowSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowSpacing.current

    val shapes: FlowShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowShapes.current

    val typography: FlowTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowTypography.current

    val state: FlowThemeState
        @Composable
        @ReadOnlyComposable
        get() = LocalFlowThemeState.current
}
