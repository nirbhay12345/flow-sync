package com.example.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimalist Surface Card with hairline border and matte background.
 */
@Composable
fun FlowCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(FlowTheme.shapes.lg),
    backgroundColor: Color = FlowTheme.colors.surface,
    borderColor: Color = FlowTheme.colors.border,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(clickableModifier)
    ) {
        content()
    }
}

enum class FlowButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    GHOST,
    DANGER
}

/**
 * Tactile minimalist button with clear hierarchy.
 */
@Composable
fun FlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FlowButtonVariant = FlowButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val (bg, contentColor, borderStroke) = when (variant) {
        FlowButtonVariant.PRIMARY -> Triple(
            colors.accent,
            colors.onAccent,
            null
        )
        FlowButtonVariant.SECONDARY -> Triple(
            colors.surfaceSubtle,
            colors.textPrimary,
            BorderStroke(1.dp, colors.border)
        )
        FlowButtonVariant.OUTLINED -> Triple(
            Color.Transparent,
            colors.textPrimary,
            BorderStroke(1.dp, colors.border)
        )
        FlowButtonVariant.GHOST -> Triple(
            Color.Transparent,
            colors.textSecondary,
            null
        )
        FlowButtonVariant.DANGER -> Triple(
            colors.error.copy(alpha = 0.15f),
            colors.error,
            BorderStroke(1.dp, colors.error.copy(alpha = 0.3f))
        )
    }

    val finalBg = if (enabled) bg else bg.copy(alpha = 0.4f)
    val finalContentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(shapes.md),
        color = finalBg,
        border = borderStroke,
        modifier = modifier.height(height)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = FlowTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = finalContentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(FlowTheme.spacing.sm))
            }
            Text(
                text = text,
                style = FlowTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = finalContentColor
            )
        }
    }
}

/**
 * Minimalist square or circular icon button.
 */
@Composable
fun FlowIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FlowTheme.colors.textSecondary,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color? = FlowTheme.colors.borderSubtle,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(FlowTheme.shapes.sm))
            .background(backgroundColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(FlowTheme.shapes.sm))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Minimalist pill segmented control with smooth transitions.
 */
@Composable
fun <T> FlowSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(shapes.md))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(shapes.md))
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val targetBg = if (isSelected) colors.accentContainer else Color.Transparent
                val animatedBg by animateColorAsState(
                    targetValue = targetBg,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "seg_bg"
                )
                val targetTextColor = if (isSelected) colors.onAccentContainer else colors.textSecondary
                val animatedTextColor by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = tween(200),
                    label = "seg_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(shapes.sm))
                        .background(animatedBg)
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = FlowTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = animatedTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Filter chip for tags, categories, or statuses.
 */
@Composable
fun FlowChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val bg = if (isSelected) colors.accentContainer else colors.surfaceElevated
    val contentColor = if (isSelected) colors.onAccentContainer else colors.textSecondary
    val borderColor = if (isSelected) colors.accent.copy(alpha = 0.5f) else colors.border

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(shapes.pill))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(shapes.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = FlowTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Minimalist category or priority badge.
 */
@Composable
fun FlowBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    hasDot: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(FlowTheme.shapes.xs))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = FlowTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

/**
 * Minimalist Metric Card for KPI displays.
 */
@Composable
fun FlowMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = FlowTheme.colors.accent
) {
    FlowCard(modifier = modifier) {
        Column(modifier = Modifier.padding(FlowTheme.spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = FlowTheme.typography.labelSmall,
                    color = FlowTheme.colors.textMuted
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(FlowTheme.spacing.xs))
            Text(
                text = value,
                style = FlowTheme.typography.headline.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = FlowTheme.colors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(FlowTheme.spacing.xxs))
                Text(
                    text = subtitle,
                    style = FlowTheme.typography.bodySmall,
                    color = FlowTheme.colors.textSecondary
                )
            }
        }
    }
}

/**
 * Live Customization Sheet allowing user to tailor the app's aesthetic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowCustomizationSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = FlowTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        FlowCustomizationContent(onDismiss = onDismiss)
    }
}

@Composable
fun FlowCustomizationContent(
    onDismiss: () -> Unit
) {
    val state = FlowTheme.state
    val colors = FlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customize Aesthetic",
                        style = FlowTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Minimalist tones & timer preferences",
                        style = FlowTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                FlowIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismiss
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Accent Palette
            Text(
                text = "ACCENT PALETTE",
                style = FlowTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(AccentTheme.values()) { theme ->
                    val isSelected = state.accentTheme == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(FlowTheme.shapes.md))
                            .background(if (isSelected) colors.surfaceSubtle else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.borderSubtle,
                                RoundedCornerShape(FlowTheme.shapes.md)
                            )
                            .clickable { state.accentTheme = theme }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(theme.previewColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = theme.title,
                            style = FlowTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Timer Style
            Text(
                text = "TIMER DIAL STYLE",
                style = FlowTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerStyle.values().forEach { style ->
                    val isSelected = state.timerStyle == style
                    FlowCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (isSelected) colors.accentContainer else colors.surfaceElevated,
                        borderColor = if (isSelected) colors.accent.copy(alpha = 0.6f) else colors.border,
                        onClick = { state.timerStyle = style }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = style.title,
                                style = FlowTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) colors.onAccentContainer else colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = style.subtitle,
                                style = FlowTheme.typography.bodySmall,
                                color = if (isSelected) colors.onAccentContainer.copy(alpha = 0.8f) else colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Focus Duration
            Text(
                text = "FOCUS INTERVAL PRESET",
                style = FlowTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 25, 45, 50, 60).forEach { mins ->
                    val isSelected = state.workMinutes == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FlowTheme.shapes.sm))
                            .background(if (isSelected) colors.accent else colors.surfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.border,
                                RoundedCornerShape(FlowTheme.shapes.sm)
                            )
                            .clickable { state.workMinutes = mins }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mins}m",
                            style = FlowTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) colors.onAccent else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FlowButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
}
