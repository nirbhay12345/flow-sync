package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FocusSession
import com.example.ui.components.PomodoroTimerDisplay
import com.example.ui.designsystem.FlowCard
import com.example.ui.designsystem.FlowIconButton
import com.example.ui.designsystem.FlowSegmentedControl
import com.example.ui.designsystem.FlowTheme
import com.example.ui.viewmodel.TimerMode
import com.example.ui.viewmodel.TimerUiState

@Composable
fun TimerScreen(
    timerState: TimerUiState,
    sessions: List<FocusSession>,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectMode: (TimerMode) -> Unit,
    onUnlinkTask: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val scrollState = rememberScrollState()
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val todayMinutes = remember(sessions) {
        sessions.filter { it.type == "focus" }.sumOf { it.durationMinutes }
    }

    val modeColor = when (timerState.mode) {
        TimerMode.FOCUS -> colors.accent
        TimerMode.SHORT_BREAK -> colors.textSecondary
        TimerMode.LONG_BREAK -> colors.priorityMedium
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = FlowTheme.spacing.xl, vertical = FlowTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector (Focus, Short Break, Long Break)
        FlowSegmentedControl(
            items = TimerMode.values().toList(),
            selectedItem = timerState.mode,
            onItemSelected = onSelectMode,
            labelProvider = { it.title },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Linked Task Card
        FlowCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTasks() }
                .testTag("linked_task_card"),
            shape = RoundedCornerShape(shapes.md),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(shapes.xs))
                            .background(if (timerState.linkedTaskId != null) colors.accentContainer else colors.surfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (timerState.linkedTaskId != null) Icons.Default.TaskAlt else Icons.Default.Link,
                            contentDescription = null,
                            tint = if (timerState.linkedTaskId != null) colors.onAccentContainer else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (timerState.linkedTaskId != null) "FOCUS TARGET" else "ACTIVE GOAL",
                            style = FlowTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Text(
                            text = timerState.linkedTaskTitle,
                            style = FlowTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }

                if (timerState.linkedTaskId != null) {
                    FlowIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Unlink Task",
                        onClick = onUnlinkTask,
                        size = 32.dp,
                        borderColor = null
                    )
                } else {
                    Text(
                        text = "Assign",
                        style = FlowTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pomodoro Timer Component
        PomodoroTimerDisplay(
            timeLeftSeconds = timerState.timeLeftSeconds,
            totalSeconds = timerState.totalSeconds,
            isRunning = timerState.isRunning,
            activeColor = modeColor,
            trackColor = colors.border,
            onStart = onToggleTimer,
            onPause = onToggleTimer,
            onReset = onResetTimer,
            onAddMinutes = { extraMinutes ->
                // Quick bump through toggle or timer
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Summary Metrics
        FlowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(shapes.lg),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$todayMinutes m",
                        style = FlowTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Focus Today",
                        style = FlowTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(colors.border)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${sessions.size}",
                        style = FlowTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sessions Logged",
                        style = FlowTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}
