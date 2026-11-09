package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.data.repository.ProductivityDataSource
import com.example.data.repository.ProductivityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TimerMode(val title: String, val defaultMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

data class TimerUiState(
    val mode: TimerMode = TimerMode.FOCUS,
    val timeLeftSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val linkedTaskId: String? = null,
    val linkedTaskTitle: String = "General Focus"
)

class ProductivityViewModel(
    private val repository: ProductivityDataSource
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.observeTasks()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habits: StateFlow<List<Habit>> = repository.observeHabits()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sessions: StateFlow<List<FocusSession>> = repository.observeSessions()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val profile: StateFlow<UserProfile?> = repository.observeUserProfile()
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun selectTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                mode = mode,
                timeLeftSeconds = mode.defaultMinutes * 60,
                totalSeconds = mode.defaultMinutes * 60,
                isRunning = false
            )
        }
    }

    fun linkTaskToTimer(task: Task) {
        _timerState.update {
            it.copy(
                linkedTaskId = task.id,
                linkedTaskTitle = task.title
            )
        }
    }

    fun unlinkTask() {
        _timerState.update {
            it.copy(
                linkedTaskId = null,
                linkedTaskTitle = "General Focus"
            )
        }
    }

    fun toggleTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _timerState.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.timeLeftSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                _timerState.update { it.copy(timeLeftSeconds = it.timeLeftSeconds - 1) }
            }
            if (_timerState.value.timeLeftSeconds <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                timeLeftSeconds = it.mode.defaultMinutes * 60,
                totalSeconds = it.mode.defaultMinutes * 60,
                isRunning = false
            )
        }
    }

    private fun onTimerFinished() {
        val currentState = _timerState.value
        val completedMinutes = currentState.totalSeconds / 60
        val type = when (currentState.mode) {
            TimerMode.FOCUS -> "focus"
            TimerMode.SHORT_BREAK -> "short_break"
            TimerMode.LONG_BREAK -> "long_break"
        }

        viewModelScope.launch {
            repository.logSession(
                taskTitle = currentState.linkedTaskTitle,
                category = "Work",
                durationMinutes = completedMinutes,
                type = type
            )

            // If a task was linked and this was a focus session, increment its completed pomodoro count
            if (currentState.mode == TimerMode.FOCUS && currentState.linkedTaskId != null) {
                val currentTask = tasks.value.firstOrNull { it.id == currentState.linkedTaskId }
                if (currentTask != null) {
                    val updated = currentTask.copy(
                        completedPomodoros = currentTask.completedPomodoros + 1
                    )
                    repository.updateTask(updated)
                }
            }
        }

        // Auto transition to break or next focus
        val nextMode = if (currentState.mode == TimerMode.FOCUS) TimerMode.SHORT_BREAK else TimerMode.FOCUS
        selectTimerMode(nextMode)
    }

    fun createTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        estimatedPomodoros: Int,
        dueDate: String?
    ) {
        viewModelScope.launch {
            repository.createTask(
                title = title,
                description = description,
                category = category,
                priority = priority,
                estimatedPomodoros = estimatedPomodoros,
                dueDate = dueDate
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun createHabit(title: String, category: String, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            repository.createHabit(title, category, targetDaysPerWeek)
        }
    }

    fun toggleHabitToday(habit: Habit) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.toggleHabitForDate(habit, todayStr)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun saveGoal(minutes: Int) {
        viewModelScope.launch {
            repository.saveUserProfile("Productivity Champion", minutes)
        }
    }
}

class ProductivityViewModelFactory(
    private val repository: ProductivityDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductivityViewModel(repository) as T
    }
}
