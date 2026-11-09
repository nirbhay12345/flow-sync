package com.example.data.repository

import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProductivityDataSource {
    fun observeTasks(): Flow<List<Task>>
    suspend fun createTask(
        title: String,
        description: String = "",
        category: String = "Work",
        priority: String = "medium",
        estimatedPomodoros: Int = 2,
        dueDate: String? = null
    ): Result<String>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>

    fun observeHabits(): Flow<List<Habit>>
    suspend fun createHabit(
        title: String,
        category: String = "Health",
        targetDaysPerWeek: Int = 7
    ): Result<String>
    suspend fun toggleHabitForDate(habit: Habit, dateStr: String): Result<Unit>
    suspend fun deleteHabit(habitId: String): Result<Unit>

    fun observeSessions(): Flow<List<FocusSession>>
    suspend fun logSession(
        taskTitle: String,
        category: String,
        durationMinutes: Int,
        type: String = "focus"
    ): Result<String>

    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(displayName: String, dailyGoalMinutes: Int): Result<Unit>
}
