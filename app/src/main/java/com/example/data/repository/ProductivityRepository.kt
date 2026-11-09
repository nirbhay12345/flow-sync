package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProductivityRepository(private val db: FirebaseFirestore) : ProductivityDataSource {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- USER PROFILE ---
    override fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid).snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: (error as? Exception ?: Exception(error)), OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun saveUserProfile(displayName: String, dailyGoalMinutes: Int): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName,
                    "dailyFocusGoalMinutes" to dailyGoalMinutes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updateMap = mapOf(
                    "displayName" to displayName,
                    "dailyFocusGoalMinutes" to dailyGoalMinutes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}")
            Result.failure(e)
        }
    }

    // --- TASKS ---
    override fun observeTasks(): Flow<List<Task>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/tasks"
        emitAll(
            db.collection("users").document(uid).collection("tasks")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Task::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: (error as? Exception ?: Exception(error)), OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun createTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        estimatedPomodoros: Int,
        dueDate: String?
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tasks").document()
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "title" to title,
            "category" to category,
            "priority" to priority,
            "estimatedPomodoros" to estimatedPomodoros,
            "completedPomodoros" to 0,
            "isCompleted" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (description.isNotBlank()) {
            payload["description"] = description
        }
        if (!dueDate.isNullOrBlank()) {
            payload["dueDate"] = dueDate
        }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        val uid = requireUserId()
        require(task.id.isNotBlank()) { "Task ID cannot be blank" }
        val docRef = db.collection("users").document(uid).collection("tasks").document(task.id)
        val updateMap = mutableMapOf<String, Any>(
            "title" to task.title,
            "category" to task.category,
            "priority" to task.priority,
            "estimatedPomodoros" to task.estimatedPomodoros,
            "completedPomodoros" to task.completedPomodoros,
            "isCompleted" to task.isCompleted,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (task.description.isNotBlank()) {
            updateMap["description"] = task.description
        }
        if (!task.dueDate.isNullOrBlank()) {
            updateMap["dueDate"] = task.dueDate
        }

        return try {
            docRef.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tasks").document(taskId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- HABITS ---
    override fun observeHabits(): Flow<List<Habit>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/habits"
        emitAll(
            db.collection("users").document(uid).collection("habits")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Habit::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: (error as? Exception ?: Exception(error)), OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun createHabit(
        title: String,
        category: String,
        targetDaysPerWeek: Int
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("habits").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "category" to category,
            "targetDaysPerWeek" to targetDaysPerWeek,
            "currentStreak" to 0,
            "bestStreak" to 0,
            "completedDates" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun toggleHabitForDate(habit: Habit, dateStr: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("habits").document(habit.id)
        val isAlreadyDone = habit.completedDates.contains(dateStr)
        val newDates = if (isAlreadyDone) {
            habit.completedDates.filter { it != dateStr }
        } else {
            (habit.completedDates + dateStr).distinct().takeLast(60)
        }

        val newStreak = if (isAlreadyDone) {
            (habit.currentStreak - 1).coerceAtLeast(0)
        } else {
            habit.currentStreak + 1
        }
        val newBest = maxOf(habit.bestStreak, newStreak)

        val updateMap = mutableMapOf<String, Any>(
            "completedDates" to newDates,
            "currentStreak" to newStreak,
            "bestStreak" to newBest,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!isAlreadyDone) {
            updateMap["lastCompletedDate"] = dateStr
        }

        return try {
            docRef.update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("habits").document(habitId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- FOCUS SESSIONS ---
    override fun observeSessions(): Flow<List<FocusSession>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/sessions"
        emitAll(
            db.collection("users").document(uid).collection("sessions")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FocusSession::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: (error as? Exception ?: Exception(error)), OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun logSession(
        taskTitle: String,
        category: String,
        durationMinutes: Int,
        type: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("sessions").document()
        val payload = mapOf(
            "userId" to uid,
            "taskTitle" to taskTitle,
            "category" to category,
            "durationMinutes" to durationMinutes,
            "type" to type,
            "completedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
