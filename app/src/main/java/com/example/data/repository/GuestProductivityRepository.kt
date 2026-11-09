package com.example.data.repository

import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GuestProductivityRepository : ProductivityDataSource {

    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _tasks = MutableStateFlow(
        listOf(
            Task(
                id = "guest_task_1",
                userId = "guest",
                title = "Review product roadmap",
                description = "Plan Q1 goals and focus blocks",
                category = "Work",
                priority = "high",
                estimatedPomodoros = 4,
                completedPomodoros = 1,
                isCompleted = false
            ),
            Task(
                id = "guest_task_2",
                userId = "guest",
                title = "Deep focus coding session",
                description = "Build Pomodoro timer in Compose",
                category = "Study",
                priority = "high",
                estimatedPomodoros = 2,
                completedPomodoros = 2,
                isCompleted = true
            ),
            Task(
                id = "guest_task_3",
                userId = "guest",
                title = "Evening 20m walk",
                description = "Clear mind and get fresh air",
                category = "Health",
                priority = "low",
                estimatedPomodoros = 1,
                completedPomodoros = 0,
                isCompleted = false
            )
        )
    )

    private val _habits = MutableStateFlow(
        listOf(
            Habit(
                id = "guest_habit_1",
                userId = "guest",
                title = "Read 20 pages",
                category = "Learning",
                targetDaysPerWeek = 7,
                currentStreak = 4,
                bestStreak = 12,
                lastCompletedDate = todayStr,
                completedDates = listOf(todayStr)
            ),
            Habit(
                id = "guest_habit_2",
                userId = "guest",
                title = "Morning Workout",
                category = "Health",
                targetDaysPerWeek = 5,
                currentStreak = 2,
                bestStreak = 8,
                lastCompletedDate = null,
                completedDates = emptyList()
            ),
            Habit(
                id = "guest_habit_3",
                userId = "guest",
                title = "Plan Tomorrow Today",
                category = "Productivity",
                targetDaysPerWeek = 7,
                currentStreak = 6,
                bestStreak = 14,
                lastCompletedDate = todayStr,
                completedDates = listOf(todayStr)
            )
        )
    )

    private val _sessions = MutableStateFlow(
        listOf(
            FocusSession(
                id = "guest_sess_1",
                userId = "guest",
                taskTitle = "Deep focus coding session",
                category = "Study",
                durationMinutes = 25,
                type = "focus"
            ),
            FocusSession(
                id = "guest_sess_2",
                userId = "guest",
                taskTitle = "Review product roadmap",
                category = "Work",
                durationMinutes = 25,
                type = "focus"
            )
        )
    )

    private val _profile = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "guest_profile",
            userId = "guest",
            displayName = "Guest Achiever",
            dailyFocusGoalMinutes = 100
        )
    )

    override fun observeTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun createTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        estimatedPomodoros: Int,
        dueDate: String?
    ): Result<String> {
        val newId = UUID.randomUUID().toString()
        val newTask = Task(
            id = newId,
            userId = "guest",
            title = title,
            description = description,
            category = category,
            priority = priority,
            estimatedPomodoros = estimatedPomodoros,
            completedPomodoros = 0,
            isCompleted = false,
            dueDate = dueDate
        )
        _tasks.value = _tasks.value + newTask
        return Result.success(newId)
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        return Result.success(Unit)
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        return Result.success(Unit)
    }

    override fun observeHabits(): Flow<List<Habit>> = _habits.asStateFlow()

    override suspend fun createHabit(
        title: String,
        category: String,
        targetDaysPerWeek: Int
    ): Result<String> {
        val newId = UUID.randomUUID().toString()
        val newHabit = Habit(
            id = newId,
            userId = "guest",
            title = title,
            category = category,
            targetDaysPerWeek = targetDaysPerWeek,
            currentStreak = 0,
            bestStreak = 0,
            completedDates = emptyList()
        )
        _habits.value = _habits.value + newHabit
        return Result.success(newId)
    }

    override suspend fun toggleHabitForDate(habit: Habit, dateStr: String): Result<Unit> {
        val isAlreadyDone = habit.completedDates.contains(dateStr)
        val newDates = if (isAlreadyDone) {
            habit.completedDates.filter { it != dateStr }
        } else {
            (habit.completedDates + dateStr).distinct().takeLast(60)
        }
        val newStreak = if (isAlreadyDone) (habit.currentStreak - 1).coerceAtLeast(0) else habit.currentStreak + 1
        val newBest = maxOf(habit.bestStreak, newStreak)

        val updated = habit.copy(
            completedDates = newDates,
            currentStreak = newStreak,
            bestStreak = newBest,
            lastCompletedDate = if (!isAlreadyDone) dateStr else habit.lastCompletedDate
        )
        _habits.value = _habits.value.map { if (it.id == habit.id) updated else it }
        return Result.success(Unit)
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        _habits.value = _habits.value.filter { it.id != habitId }
        return Result.success(Unit)
    }

    override fun observeSessions(): Flow<List<FocusSession>> = _sessions.asStateFlow()

    override suspend fun logSession(
        taskTitle: String,
        category: String,
        durationMinutes: Int,
        type: String
    ): Result<String> {
        val newId = UUID.randomUUID().toString()
        val newSession = FocusSession(
            id = newId,
            userId = "guest",
            taskTitle = taskTitle,
            category = category,
            durationMinutes = durationMinutes,
            type = type
        )
        _sessions.value = _sessions.value + newSession
        return Result.success(newId)
    }

    override fun observeUserProfile(): Flow<UserProfile?> = _profile.asStateFlow()

    override suspend fun saveUserProfile(displayName: String, dailyGoalMinutes: Int): Result<Unit> {
        _profile.value = _profile.value?.copy(
            displayName = displayName,
            dailyFocusGoalMinutes = dailyGoalMinutes
        ) ?: UserProfile(
            id = "guest_profile",
            userId = "guest",
            displayName = displayName,
            dailyFocusGoalMinutes = dailyGoalMinutes
        )
        return Result.success(Unit)
    }
}
