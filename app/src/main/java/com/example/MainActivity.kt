package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.GuestProductivityRepository
import com.example.data.repository.ProductivityDataSource
import com.example.data.repository.ProductivityRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.designsystem.FlowBadge
import com.example.ui.designsystem.FlowButton
import com.example.ui.designsystem.FlowButtonVariant
import com.example.ui.designsystem.FlowCustomizationSheet
import com.example.ui.designsystem.FlowTheme
import com.example.ui.designsystem.FlowThemeState
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ProductivityViewModel
import com.example.ui.viewmodel.ProductivityViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class AppTab(val title: String, val icon: ImageVector) {
    TIMER("Focus", Icons.Default.Timer),
    TASKS("Tasks", Icons.Default.Checklist),
    HABITS("Habits", Icons.Default.LocalFireDepartment),
    ANALYTICS("Insights", Icons.Default.Insights)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState = remember { FlowThemeState() }
            MyApplicationTheme(themeState = themeState) {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isGuestMode = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser != null) {
        val uid = currentUser!!.uid
        key(uid) {
            MainFeatureScreen(
                currentUserId = uid,
                userEmail = currentUser?.email ?: "user@flowpulse.app",
                userDisplayName = currentUser?.displayName ?: "Champion",
                isGuestMode = false,
                onExitGuest = { isGuestMode = false }
            )
        }
    } else if (isGuestMode) {
        key("guest_session") {
            MainFeatureScreen(
                currentUserId = "guest_user",
                userEmail = "Guest Mode (Offline)",
                userDisplayName = "Guest",
                isGuestMode = true,
                onExitGuest = { isGuestMode = false }
            )
        }
    } else {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                isGuestMode = false
            },
            onGuestLogin = {
                isGuestMode = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeatureScreen(
    currentUserId: String,
    userEmail: String,
    userDisplayName: String,
    isGuestMode: Boolean = false,
    onExitGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val repository: ProductivityDataSource = remember(currentUserId, isGuestMode) {
        if (isGuestMode) {
            GuestProductivityRepository()
        } else {
            ProductivityRepository(context)
        }
    }
    val viewModel: ProductivityViewModel = viewModel(
        key = "${currentUserId}_$isGuestMode",
        factory = ProductivityViewModelFactory(repository)
    )

    var currentTab by remember { mutableStateOf(AppTab.TIMER) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }

    val colors = FlowTheme.colors
    val shapes = FlowTheme.shapes

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()

    BackHandler(enabled = currentTab != AppTab.TIMER) {
        currentTab = AppTab.TIMER
    }

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
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = colors.textPrimary
                        )
                        if (isGuestMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            FlowBadge(
                                text = "GUEST",
                                color = colors.priorityMedium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCustomizationSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Aesthetic",
                            tint = colors.textSecondary
                        )
                    }

                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = if (isGuestMode) "Exit Guest Mode" else "Sign Out",
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
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = colors.surface,
                    tonalElevation = 0.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
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
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                indicatorColor = colors.accentContainer,
                                unselectedIconColor = colors.textMuted,
                                unselectedTextColor = colors.textMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
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
            Crossfade(targetState = currentTab, label = "tab_fade") { tab ->
                when (tab) {
                    AppTab.TIMER -> TimerScreen(
                        timerState = timerState,
                        sessions = sessions,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        onSelectMode = { viewModel.selectTimerMode(it) },
                        onUnlinkTask = { viewModel.unlinkTask() },
                        onNavigateToTasks = { currentTab = AppTab.TASKS }
                    )
                    AppTab.TASKS -> TasksScreen(
                        tasks = tasks,
                        onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onStartFocusForTask = { task ->
                            viewModel.linkTaskToTimer(task)
                            currentTab = AppTab.TIMER
                        },
                        onCreateTask = { title, desc, cat, priority, pomodoros, dueDate ->
                            viewModel.createTask(title, desc, cat, priority, pomodoros, dueDate)
                        }
                    )
                    AppTab.HABITS -> HabitsScreen(
                        habits = habits,
                        onToggleHabitToday = { viewModel.toggleHabitToday(it) },
                        onDeleteHabit = { viewModel.deleteHabit(it) },
                        onCreateHabit = { title, category, days ->
                            viewModel.createHabit(title, category, days)
                        }
                    )
                    AppTab.ANALYTICS -> AnalyticsScreen(
                        tasks = tasks,
                        habits = habits,
                        sessions = sessions,
                        profile = profile
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = if (isGuestMode) "Exit Guest Mode" else "Sign Out",
                    style = FlowTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = if (isGuestMode) {
                        "You are currently using FlowPulse in offline Guest Mode. You can sign in with your Google account anytime to back up your productivity metrics and sync across devices."
                    } else {
                        "Signed in as $userEmail ($userDisplayName). Are you sure you want to sign out?"
                    },
                    style = FlowTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                FlowButton(
                    text = if (isGuestMode) "Exit to Sign In" else "Sign Out",
                    variant = FlowButtonVariant.DANGER,
                    onClick = {
                        showSignOutDialog = false
                        if (isGuestMode) {
                            onExitGuest()
                        } else {
                            signOut(credentialManager, coroutineScope)
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(shapes.lg)
        )
    }

    // Live Customization Modal Sheet
    FlowCustomizationSheet(
        isOpen = showCustomizationSheet,
        onDismiss = { showCustomizationSheet = false }
    )
}
