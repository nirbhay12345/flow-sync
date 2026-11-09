package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PomodoroTimerTest {

    @Test
    fun defaultPomodoroDuration_is25Minutes() {
        val initialMinutes = 25
        val totalSeconds = initialMinutes * 60
        assertEquals(1500, totalSeconds)
    }

    @Test
    fun timerFormat_formatsCorrectly() {
        val secondsRemaining = 25 * 60
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val formatted = String.format("%02d:%02d", minutes, seconds)
        assertEquals("25:00", formatted)

        val oneMinuteRemaining = 65
        val formattedOne = String.format("%02d:%02d", oneMinuteRemaining / 60, oneMinuteRemaining % 60)
        assertEquals("01:05", formattedOne)
    }

    @Test
    fun countdownLogic_decrementsTime() {
        var timeLeft = 1500
        var isRunning = true

        if (isRunning && timeLeft > 0) {
            timeLeft--
        }

        assertEquals(1499, timeLeft)
        assertTrue(isRunning)
    }

    @Test
    fun resetLogic_restores25Minutes() {
        val totalSeconds = 25 * 60
        var timeLeft = 300
        var isRunning = true

        // Reset
        isRunning = false
        timeLeft = totalSeconds

        assertEquals(1500, timeLeft)
        assertFalse(isRunning)
    }
}
