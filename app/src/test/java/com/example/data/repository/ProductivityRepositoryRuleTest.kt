package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductivityRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createTask_validPayload_createsDocumentAndReturnsId() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ProductivityRepository(firestore)

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createTask("Finish Architecture Review", priority = "high")
    }
    assertTrue(createResult.isSuccess)
    val taskId = createResult.getOrThrow()
    assertTrue(taskId.isNotEmpty())
  }

  @Test
  fun observeTasks_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ProductivityRepository(firestore)
    val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createTask("Deep Work Focus Block").getOrThrow()
    }

    val emittedTasks = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeTasks().first { list -> list.any { it.id == taskId } }
    }
    assertTrue(emittedTasks.map { it.id }.contains(taskId))
  }

  @Test
  fun createHabit_andToggle_updatesStreak() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ProductivityRepository(firestore)
    val habitId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createHabit("Daily 30m Workout", "Health", 5).getOrThrow()
    }

    val habit = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeHabits().first { list -> list.any { it.id == habitId } }.first { it.id == habitId }
    }
    assertEquals(0, habit.currentStreak)

    val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.toggleHabitForDate(habit, "2026-11-08")
    }
    assertTrue(toggleResult.isSuccess)

    val updatedHabit = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeHabits().first { list ->
        list.any { it.id == habitId && it.currentStreak == 1 }
      }.first { it.id == habitId }
    }
    assertTrue(updatedHabit.completedDates.contains("2026-11-08"))
  }

  @Test
  fun logSession_success() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ProductivityRepository(firestore)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.logSession("Complete Project Spec", "Work", 25, "focus")
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun observeTasks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = ProductivityRepository(firestore)

    var caught = false
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeTasks().first()
      }
    } catch (e: Exception) {
      caught = true
      assertTrue(e.message?.contains("User must be signed in with Google") == true)
    }
    assertTrue(caught)
  }

  private companion object {
    const val ALICE_EMAIL = "alice@flowpulse.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
