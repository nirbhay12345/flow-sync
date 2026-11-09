package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.designsystem.FlowCard
import com.example.ui.designsystem.FlowTheme
import com.example.ui.designsystem.TimerStyle
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * A standalone, self-contained Pomodoro Timer Compose component that allows users
 * to start, pause, and reset countdown with minimalist design and customizable styles.
 */
@Composable
fun PomodoroTimer(
    modifier: Modifier = Modifier,
    initialMinutes: Int = 25,
    dialSize: Dp = 260.dp,
    activeColor: Color = FlowTheme.colors.accent,
    trackColor: Color = FlowTheme.colors.border,
    onSessionFinished: () -> Unit = {}
) {
    var totalSeconds by remember(initialMinutes) { mutableIntStateOf(initialMinutes * 60) }
    var timeLeftSeconds by remember(initialMinutes) { mutableIntStateOf(initialMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeLeftSeconds) {
        if (isRunning && timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                isRunning = false
                onSessionFinished()
            }
        }
    }

    PomodoroTimerDisplay(
        modifier = modifier.testTag("pomodoro_timer_component"),
        timeLeftSeconds = timeLeftSeconds,
        totalSeconds = totalSeconds,
        isRunning = isRunning,
        dialSize = dialSize,
        activeColor = activeColor,
        trackColor = trackColor,
        onStart = { isRunning = true },
        onPause = { isRunning = false },
        onReset = {
            isRunning = false
            timeLeftSeconds = totalSeconds
        },
        onAddMinutes = { extraMinutes ->
            timeLeftSeconds += extraMinutes * 60
            if (timeLeftSeconds > totalSeconds) {
                totalSeconds = timeLeftSeconds
            }
        }
    )
}

/**
 * Stateless Pomodoro Timer component supporting external state management and customizable aesthetic.
 */
@Composable
fun PomodoroTimerDisplay(
    modifier: Modifier = Modifier,
    timeLeftSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    dialSize: Dp = 250.dp,
    activeColor: Color = FlowTheme.colors.accent,
    trackColor: Color = FlowTheme.colors.border,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onAddMinutes: ((Int) -> Unit)? = null
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes
    val timerStyle = FlowTheme.state.timerStyle

    val progress = if (totalSeconds > 0) {
        timeLeftSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "pomodoro_timer_progress"
    )

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    FlowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(shapes.xl),
        backgroundColor = colors.surface,
        borderColor = colors.border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (timerStyle) {
                TimerStyle.MINIMAL_RING -> {
                    // Refined hairline arc visualizer
                    Box(
                        modifier = Modifier.size(dialSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            val trackStrokeWidth = 3.dp.toPx()
                            val activeStrokeWidth = 5.dp.toPx()

                            // Hairline Track
                            drawCircle(
                                color = trackColor,
                                style = Stroke(width = trackStrokeWidth)
                            )

                            // Active Arc
                            drawArc(
                                color = activeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = activeStrokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = FlowTheme.typography.timerDigits,
                                color = colors.textPrimary,
                                modifier = Modifier.testTag("pomodoro_time_display")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = when {
                                    isRunning -> "FOCUSING"
                                    timeLeftSeconds == 0 -> "COMPLETED"
                                    timeLeftSeconds < totalSeconds -> "PAUSED"
                                    else -> "READY"
                                },
                                style = FlowTheme.typography.labelSmall,
                                color = if (isRunning) activeColor else colors.textMuted
                            )
                        }
                    }
                }
                TimerStyle.ZEN_NUMERIC -> {
                    // Pure typographic layout with clean baseline indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Text(
                            text = when {
                                isRunning -> "FOCUS SESSION"
                                timeLeftSeconds == 0 -> "SESSION COMPLETE"
                                timeLeftSeconds < totalSeconds -> "SESSION PAUSED"
                                else -> "FOCUS SESSION"
                            },
                            style = FlowTheme.typography.labelSmall,
                            color = if (isRunning) activeColor else colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formattedTime,
                            style = FlowTheme.typography.timerDigits.copy(fontSize = 68.sp),
                            color = colors.textPrimary,
                            modifier = Modifier.testTag("pomodoro_time_display")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Minimalist hairline baseline progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(FlowTheme.shapes.pill))
                                .background(colors.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(FlowTheme.shapes.pill))
                                    .background(activeColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons: Minimal tactile Reset, Play/Pause, and Quick Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(shapes.md))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, RoundedCornerShape(shapes.md))
                        .clickable { onReset() }
                        .testTag("pomodoro_reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Countdown",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play / Pause Button - Minimalist Primary
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(shapes.md))
                        .background(activeColor)
                        .clickable {
                            if (isRunning) onPause() else onStart()
                        }
                        .testTag("pomodoro_start_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause Pomodoro" else "Start Pomodoro",
                            tint = colors.onAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Pause" else "Start",
                            style = FlowTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onAccent
                        )
                    }
                }

                if (onAddMinutes != null) {
                    Spacer(modifier = Modifier.width(16.dp))

                    // +5 Min Quick Adjustment Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(shapes.md))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, RoundedCornerShape(shapes.md))
                            .clickable { onAddMinutes(5) }
                            .testTag("pomodoro_add_5m_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "+5 Min",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "5",
                                style = FlowTheme.typography.monoLabel.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
