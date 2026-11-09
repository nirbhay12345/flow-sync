package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthScreenContent
import com.example.ui.designsystem.AccentTheme
import com.example.ui.designsystem.FlowBadge
import com.example.ui.designsystem.FlowCustomizationSheet
import com.example.ui.designsystem.FlowTheme
import com.example.ui.designsystem.FlowThemeState
import com.example.ui.designsystem.ThemeMode
import com.example.ui.designsystem.TimerStyle
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TimerMode
import com.example.ui.viewmodel.TimerUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class ScreenshotGeneratorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private val outputDir = File("../docs/screenshots").apply { mkdirs() }

    private fun captureAndSave(fileName: String) {
        composeTestRule.waitForIdle()
        val view = composeTestRule.activity.window.decorView
        view.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(2340, android.view.View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, 1080, 2340)

        val bitmap = Bitmap.createBitmap(1080, 2340, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val targetFile = File(outputDir, fileName)
        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("SUCCESS: Captured screenshot -> ${targetFile.absolutePath} (${targetFile.length()} bytes)")
    }

    @Test
    fun capture01_AuthScreen() {
        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FlowTheme.colors.background)
                ) {
                    AuthScreenContent(
                        isLoading = false,
                        onGoogleSignIn = {},
                        onGuestLogin = {}
                    )
                }
            }
        }
        captureAndSave("01_auth_screen.png")
    }

    @Test
    fun capture02_TimerScreen_NordicSage() {
        val timerState = TimerUiState(
            mode = TimerMode.FOCUS,
            timeLeftSeconds = 21 * 60 + 34,
            totalSeconds = 25 * 60,
            isRunning = false,
            linkedTaskId = "task_1",
            linkedTaskTitle = "Design System Architecture & Tokens"
        )
        val sessions = listOf(
            FocusSession(id = "1", taskTitle = "Deep Work Session 1", durationMinutes = 25, type = "focus"),
            FocusSession(id = "2", taskTitle = "Roadmap Strategy", durationMinutes = 25, type = "focus"),
            FocusSession(id = "3", taskTitle = "API Client Refactor", durationMinutes = 25, type = "focus")
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                ScreenFrame(activeTab = AppTab.TIMER) {
                    TimerScreen(
                        timerState = timerState,
                        sessions = sessions,
                        onToggleTimer = {},
                        onResetTimer = {},
                        onSelectMode = {},
                        onUnlinkTask = {},
                        onNavigateToTasks = {}
                    )
                }
            }
        }
        captureAndSave("02_timer_screen_sage.png")
    }

    @Test
    fun capture03_TimerScreen_ZenTypographic() {
        val timerState = TimerUiState(
            mode = TimerMode.FOCUS,
            timeLeftSeconds = 18 * 60 + 20,
            totalSeconds = 25 * 60,
            isRunning = false,
            linkedTaskId = "task_2",
            linkedTaskTitle = "Compose UI Performance Audit"
        )
        val sessions = listOf(
            FocusSession(id = "1", taskTitle = "Audit", durationMinutes = 25, type = "focus")
        )

        composeTestRule.setContent {
            MyApplicationTheme(
                themeState = FlowThemeState(
                    initialAccent = AccentTheme.TERRACOTTA,
                    initialTimerStyle = TimerStyle.ZEN_NUMERIC
                )
            ) {
                ScreenFrame(activeTab = AppTab.TIMER) {
                    TimerScreen(
                        timerState = timerState,
                        sessions = sessions,
                        onToggleTimer = {},
                        onResetTimer = {},
                        onSelectMode = {},
                        onUnlinkTask = {},
                        onNavigateToTasks = {}
                    )
                }
            }
        }
        captureAndSave("03_timer_zen_terracotta.png")
    }

    @Test
    fun capture04_TasksScreen() {
        val tasks = listOf(
            Task(
                id = "1",
                title = "Design System Architecture & Tokens",
                description = "Define semantic typography, spacing, and accent palettes",
                category = "Work",
                priority = "high",
                estimatedPomodoros = 4,
                completedPomodoros = 3,
                isCompleted = false
            ),
            Task(
                id = "2",
                title = "Implement offline SQLite repository",
                description = "Offline-first data sync with conflict resolution",
                category = "Study",
                priority = "high",
                estimatedPomodoros = 3,
                completedPomodoros = 1,
                isCompleted = false
            ),
            Task(
                id = "3",
                title = "Compose UI Performance Profiling",
                description = "Ensure 120fps smooth scrolling with Layout Inspector",
                category = "Work",
                priority = "medium",
                estimatedPomodoros = 2,
                completedPomodoros = 2,
                isCompleted = true
            ),
            Task(
                id = "4",
                title = "Morning 30m mindful jog",
                description = "Aerobic base building before deep work blocks",
                category = "Health",
                priority = "low",
                estimatedPomodoros = 1,
                completedPomodoros = 1,
                isCompleted = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                ScreenFrame(activeTab = AppTab.TASKS) {
                    TasksScreen(
                        tasks = tasks,
                        onToggleTaskCompletion = {},
                        onDeleteTask = {},
                        onStartFocusForTask = {},
                        onCreateTask = { _, _, _, _, _, _ -> }
                    )
                }
            }
        }
        captureAndSave("04_tasks_screen.png")
    }

    @Test
    fun capture05_HabitsScreen() {
        val habits = listOf(
            Habit(
                id = "1",
                title = "Read 25 Pages Architecture & Systems",
                category = "Learning",
                targetDaysPerWeek = 7,
                currentStreak = 14,
                bestStreak = 28,
                completedDates = listOf("2026-11-09", "2026-11-08", "2026-11-07", "2026-11-06", "2026-11-05")
            ),
            Habit(
                id = "2",
                title = "Morning High-Intensity Workout",
                category = "Health",
                targetDaysPerWeek = 5,
                currentStreak = 6,
                bestStreak = 18,
                completedDates = listOf("2026-11-09", "2026-11-08", "2026-11-07")
            ),
            Habit(
                id = "3",
                title = "Plan Tomorrow's MITs (Most Important Tasks)",
                category = "Productivity",
                targetDaysPerWeek = 7,
                currentStreak = 21,
                bestStreak = 21,
                completedDates = listOf("2026-11-08", "2026-11-07", "2026-11-06", "2026-11-05")
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                ScreenFrame(activeTab = AppTab.HABITS) {
                    HabitsScreen(
                        habits = habits,
                        onToggleHabitToday = {},
                        onDeleteHabit = {},
                        onCreateHabit = { _, _, _ -> }
                    )
                }
            }
        }
        captureAndSave("05_habits_screen.png")
    }

    @Test
    fun capture06_AnalyticsScreen() {
        val tasks = listOf(
            Task(id = "1", isCompleted = true),
            Task(id = "2", isCompleted = true),
            Task(id = "3", isCompleted = false)
        )
        val habits = listOf(
            Habit(id = "1", completedDates = listOf("2026-11-09")),
            Habit(id = "2", completedDates = listOf("2026-11-09")),
            Habit(id = "3", completedDates = emptyList())
        )
        val sessions = listOf(
            FocusSession(id = "1", durationMinutes = 25, type = "focus"),
            FocusSession(id = "2", durationMinutes = 25, type = "focus"),
            FocusSession(id = "3", durationMinutes = 25, type = "focus"),
            FocusSession(id = "4", durationMinutes = 25, type = "focus")
        )
        val profile = UserProfile(displayName = "Architect", dailyFocusGoalMinutes = 120)

        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                ScreenFrame(activeTab = AppTab.ANALYTICS) {
                    AnalyticsScreen(
                        tasks = tasks,
                        habits = habits,
                        sessions = sessions,
                        profile = profile
                    )
                }
            }
        }
        captureAndSave("06_analytics_screen.png")
    }

    @Test
    fun capture07_CustomizationSheet() {
        val timerState = TimerUiState(
            mode = TimerMode.FOCUS,
            timeLeftSeconds = 25 * 60,
            totalSeconds = 25 * 60,
            isRunning = false,
            linkedTaskId = "1",
            linkedTaskTitle = "Design System Refactoring"
        )
        val sessions = listOf(
            FocusSession(id = "1", taskTitle = "Refactoring", durationMinutes = 25, type = "focus")
        )

        composeTestRule.setContent {
            MyApplicationTheme(themeState = FlowThemeState(initialAccent = AccentTheme.SAGE)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScreenFrame(activeTab = AppTab.TIMER) {
                        TimerScreen(
                            timerState = timerState,
                            sessions = sessions,
                            onToggleTimer = {},
                            onResetTimer = {},
                            onSelectMode = {},
                            onUnlinkTask = {},
                            onNavigateToTasks = {}
                        )
                    }

                    // Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                    )

                    // Bottom Sheet container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(FlowTheme.colors.surface)
                            .border(1.dp, FlowTheme.colors.border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    ) {
                        com.example.ui.designsystem.FlowCustomizationContent(onDismiss = {})
                    }
                }
            }
        }
        captureAndSave("07_customization_sheet.png")
    }

    @Test
    fun capture08_LightPaperTheme() {
        val timerState = TimerUiState(
            mode = TimerMode.FOCUS,
            timeLeftSeconds = 25 * 60,
            totalSeconds = 25 * 60,
            isRunning = false,
            linkedTaskId = "1",
            linkedTaskTitle = "Deep Work: Clean Architecture"
        )
        val sessions = listOf(
            FocusSession(id = "1", durationMinutes = 25, type = "focus")
        )

        composeTestRule.setContent {
            MyApplicationTheme(
                themeState = FlowThemeState(
                    initialAccent = AccentTheme.COBALT,
                    initialMode = ThemeMode.LIGHT
                )
            ) {
                ScreenFrame(activeTab = AppTab.TIMER) {
                    TimerScreen(
                        timerState = timerState,
                        sessions = sessions,
                        onToggleTimer = {},
                        onResetTimer = {},
                        onSelectMode = {},
                        onUnlinkTask = {},
                        onNavigateToTasks = {}
                    )
                }
            }
        }
        captureAndSave("08_timer_light_paper.png")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenFrame(
    activeTab: AppTab,
    content: @Composable () -> Unit
) {
    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(shapes.xs))
                                    .background(colors.accentContainer)
                                    .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(shapes.xs)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = colors.onAccentContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FlowPulse",
                                style = FlowTheme.typography.titleLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FlowBadge(
                                text = "GUEST",
                                color = colors.priorityMedium
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Customize Aesthetic",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = colors.textSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colors.background
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(0.dp))
                ) {
                    NavigationBar(
                        containerColor = colors.surface,
                        tonalElevation = 0.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {},
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = FlowTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    selectedTextColor = colors.accent,
                                    indicatorColor = colors.accentContainer,
                                    unselectedIconColor = colors.textMuted,
                                    unselectedTextColor = colors.textMuted
                                )
                            )
                        }
                    }
                }
            },
            containerColor = colors.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content()
            }
        }
    }
}
