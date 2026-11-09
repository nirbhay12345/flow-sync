package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.ui.designsystem.FlowBadge
import com.example.ui.designsystem.FlowButton
import com.example.ui.designsystem.FlowCard
import com.example.ui.designsystem.FlowIconButton
import com.example.ui.designsystem.FlowTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onToggleHabitToday: (Habit) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onCreateHabit: (String, String, Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val last7Days = remember {
        val list = mutableListOf<Pair<String, String>>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 6 downTo 0) {
            val d = Calendar.getInstance()
            d.add(Calendar.DAY_OF_YEAR, -i)
            list.add(Pair(dateFormat.format(d.time), dayFormat.format(d.time).take(1)))
        }
        list
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = colors.accent,
                contentColor = colors.onAccent,
                shape = RoundedCornerShape(shapes.md),
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Habit")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header stats
            val completedTodayCount = habits.count { it.completedDates.contains(todayStr) }
            val activeStreaksCount = habits.count { it.currentStreak > 0 }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habit Streaks",
                        style = FlowTheme.typography.headline.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedTodayCount/${habits.size} done today • $activeStreaksCount active streaks",
                        style = FlowTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(shapes.md))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.border, RoundedCornerShape(shapes.md)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No habits tracked yet",
                            style = FlowTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Build discipline with daily routines like 'Read 20 mins' or 'Morning Run'.",
                            style = FlowTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            todayStr = todayStr,
                            last7Days = last7Days,
                            onToggle = { onToggleHabitToday(habit) },
                            onDelete = { onDeleteHabit(habit.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, days ->
                onCreateHabit(title, category, days)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    todayStr: String,
    last7Days: List<Pair<String, String>>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isDoneToday = habit.completedDates.contains(todayStr)
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    FlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_item_${habit.id}"),
        shape = RoundedCornerShape(shapes.lg),
        backgroundColor = colors.surface,
        borderColor = colors.border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = FlowTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlowBadge(
                            text = habit.category,
                            color = colors.textSecondary,
                            hasDot = false
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = colors.priorityMedium,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${habit.currentStreak}d streak",
                                style = FlowTheme.typography.monoLabel.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.priorityMedium
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Today Check-in Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(shapes.sm))
                            .background(if (isDoneToday) colors.accentContainer else colors.surfaceElevated)
                            .border(
                                1.dp,
                                if (isDoneToday) colors.accent else colors.border,
                                RoundedCornerShape(shapes.sm)
                            )
                            .clickable { onToggle() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("toggle_habit_${habit.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDoneToday) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.onAccentContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Done",
                                    style = FlowTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onAccentContainer
                                )
                            } else {
                                Text(
                                    text = "Log Today",
                                    style = FlowTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    FlowIconButton(
                        icon = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Habit",
                        onClick = onDelete,
                        tint = colors.textMuted,
                        size = 32.dp,
                        borderColor = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-day Visual Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                last7Days.forEach { (dateKey, dayLabel) ->
                    val isCompleted = habit.completedDates.contains(dateKey)
                    val isToday = dateKey == todayStr

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayLabel,
                            style = FlowTheme.typography.labelSmall.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isToday) colors.accent else colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) colors.accent else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isCompleted) colors.accent else colors.border,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.onAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, targetDays: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Health") }
    var targetDays by remember { mutableIntStateOf(7) }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val categories = listOf("Health", "Mindset", "Learning", "Productivity", "Fitness")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Habit Streak",
                style = FlowTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title *") },
                    placeholder = { Text("e.g. Read 20 mins") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        focusedLabelColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(shapes.sm),
                    modifier = Modifier.fillMaxWidth().testTag("habit_title_input")
                )

                Text(
                    text = "CATEGORY",
                    style = FlowTheme.typography.labelSmall,
                    color = colors.textMuted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(shapes.xs))
                                .background(if (isSelected) colors.accentContainer else colors.surfaceElevated)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(shapes.xs))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = FlowTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) colors.onAccentContainer else colors.textSecondary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET FREQUENCY",
                        style = FlowTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FlowIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Decrease",
                            onClick = { if (targetDays > 1) targetDays-- },
                            size = 28.dp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "$targetDays d/wk",
                            style = FlowTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        FlowIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Increase",
                            onClick = { if (targetDays < 7) targetDays++ },
                            size = 28.dp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            FlowButton(
                text = "Create Habit",
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), category, targetDays)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_habit_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(shapes.lg)
    )
}
