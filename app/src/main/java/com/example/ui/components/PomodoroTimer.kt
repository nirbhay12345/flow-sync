package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowViolet
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * A standalone, self-contained Pomodoro Timer Compose component that allows users
 * to start, pause, and reset a 25-minute countdown.
 */
@Composable
fun PomodoroTimer(
    modifier: Modifier = Modifier,
    initialMinutes: Int = 25,
    dialSize: Dp = 260.dp,
    activeColor: Color = FlowCyan,
    trackColor: Color = DarkBorder,
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
 * Stateless Pomodoro Timer component supporting external state management.
 */
@Composable
fun PomodoroTimerDisplay(
    modifier: Modifier = Modifier,
    timeLeftSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    dialSize: Dp = 260.dp,
    activeColor: Color = FlowCyan,
    trackColor: Color = DarkBorder,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onAddMinutes: ((Int) -> Unit)? = null
) {
    val progress = if (totalSeconds > 0) {
        timeLeftSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "pomodoro_timer_progress"
    )

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Dial Visualizer
            Box(
                modifier = Modifier.size(dialSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()

                    // Background Track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Countdown Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(activeColor, FlowViolet, activeColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 54.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("pomodoro_time_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            isRunning -> "FOCUS TIME"
                            timeLeftSeconds == 0 -> "SESSION COMPLETE"
                            timeLeftSeconds < totalSeconds -> "PAUSED"
                            else -> "25 MIN POMODORO"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = if (isRunning) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons: Reset, Start/Pause, and Optional Quick Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable { onReset() }
                        .testTag("pomodoro_reset_button"),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset 25-Minute Countdown",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Play / Pause Button
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isRunning) onPause() else onStart()
                        }
                        .testTag("pomodoro_start_pause_button"),
                    shape = CircleShape,
                    color = activeColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause Pomodoro" else "Start Pomodoro",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                if (onAddMinutes != null) {
                    Spacer(modifier = Modifier.width(24.dp))

                    // +5 Min Quick Bump Button
                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable { onAddMinutes(5) }
                            .testTag("pomodoro_add_5m_button"),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "+5 Min",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "5m",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
