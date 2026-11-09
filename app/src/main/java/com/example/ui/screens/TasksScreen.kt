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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Task
import com.example.ui.designsystem.FlowBadge
import com.example.ui.designsystem.FlowButton
import com.example.ui.designsystem.FlowCard
import com.example.ui.designsystem.FlowChip
import com.example.ui.designsystem.FlowIconButton
import com.example.ui.designsystem.FlowTheme

@Composable
fun TasksScreen(
    tasks: List<Task>,
    onToggleTaskCompletion: (Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onStartFocusForTask: (Task) -> Unit,
    onCreateTask: (String, String, String, String, Int, String?) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val filters = listOf("All", "In Progress", "Completed", "Work", "Personal", "Study", "High Priority")

    val filteredTasks = tasks.filter { task ->
        when (selectedFilter) {
            "All" -> true
            "In Progress" -> !task.isCompleted
            "Completed" -> task.isCompleted
            "Work" -> task.category.equals("Work", ignoreCase = true)
            "Personal" -> task.category.equals("Personal", ignoreCase = true)
            "Study" -> task.category.equals("Study", ignoreCase = true)
            "High Priority" -> task.priority.equals("high", ignoreCase = true)
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = colors.accent,
                contentColor = colors.onAccent,
                shape = RoundedCornerShape(shapes.md),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Task")
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
            val completedCount = tasks.count { it.isCompleted }
            val pendingCount = tasks.size - completedCount

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Priority Tasks",
                        style = FlowTheme.typography.headline.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$pendingCount active • $completedCount completed",
                        style = FlowTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                items(filters) { filter ->
                    FlowChip(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
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
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (tasks.isEmpty()) "No tasks created yet" else "No tasks matching '$selectedFilter'",
                            style = FlowTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to queue your next deep work session.",
                            style = FlowTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { onToggleTaskCompletion(task) },
                            onDelete = { onDeleteTask(task.id) },
                            onStartFocus = { onStartFocusForTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, cat, priority, pomodoros, dueDate ->
                onCreateTask(title, desc, cat, priority, pomodoros, dueDate)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val priorityColor = when (task.priority.lowercase()) {
        "high" -> colors.priorityHigh
        "low" -> colors.priorityLow
        else -> colors.priorityMedium
    }

    FlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(shapes.md),
        backgroundColor = if (task.isCompleted) colors.surface.copy(alpha = 0.5f) else colors.surface,
        borderColor = if (task.isCompleted) colors.borderSubtle else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Circle - Minimalist tactile
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) colors.accent else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (task.isCompleted) colors.accent else colors.border,
                        CircleShape
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = colors.onAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = FlowTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) colors.textMuted else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = FlowTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badges Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FlowBadge(
                        text = task.priority.uppercase(),
                        color = priorityColor
                    )

                    FlowBadge(
                        text = task.category,
                        color = colors.textSecondary,
                        hasDot = false
                    )

                    Text(
                        text = "${task.completedPomodoros}/${task.estimatedPomodoros} pom",
                        style = FlowTheme.typography.monoLabel,
                        color = colors.textMuted
                    )
                }
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!task.isCompleted) {
                    FlowIconButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Focus on this Task",
                        onClick = onStartFocus,
                        tint = colors.accent,
                        backgroundColor = colors.accentContainer,
                        borderColor = null,
                        size = 32.dp,
                        modifier = Modifier.testTag("focus_task_${task.id}")
                    )
                }

                FlowIconButton(
                    icon = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    onClick = onDelete,
                    tint = colors.textMuted,
                    backgroundColor = Color.Transparent,
                    borderColor = null,
                    size = 32.dp,
                    modifier = Modifier.testTag("delete_task_${task.id}")
                )
            }
        }
    }
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: String, priority: String, pomodoros: Int, dueDate: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("medium") }
    var pomodoros by remember { mutableIntStateOf(2) }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val categories = listOf("Work", "Personal", "Study", "Health", "Creative")
    val priorities = listOf("low", "medium", "high")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Focus Task",
                style = FlowTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        focusedLabelColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(shapes.sm),
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        focusedLabelColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(shapes.sm),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category chips
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

                // Priority selector
                Text(
                    text = "PRIORITY",
                    style = FlowTheme.typography.labelSmall,
                    color = colors.textMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val pColor = when (p) {
                            "high" -> colors.priorityHigh
                            "low" -> colors.priorityLow
                            else -> colors.priorityMedium
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(shapes.xs))
                                .background(if (isSelected) pColor.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .border(1.dp, if (isSelected) pColor else colors.border, RoundedCornerShape(shapes.xs))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.uppercase(),
                                style = FlowTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) pColor else colors.textSecondary
                            )
                        }
                    }
                }

                // Estimated Pomodoros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTIMATED POMODOROS",
                        style = FlowTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FlowIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Decrease",
                            onClick = { if (pomodoros > 1) pomodoros-- },
                            size = 28.dp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "$pomodoros",
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
                            onClick = { if (pomodoros < 12) pomodoros++ },
                            size = 28.dp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            FlowButton(
                text = "Create Task",
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), category, priority, pomodoros, null)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_task_button")
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
