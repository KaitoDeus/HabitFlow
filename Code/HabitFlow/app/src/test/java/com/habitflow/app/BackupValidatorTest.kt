package com.habitflow.app

import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidatorTest {

    @Test
    fun testValidPayloadReturnsValid() {
        val payload = BackupData(
            version = 2,
            habits = listOf(HabitEntity(id = "h1", name = "Uống nước")),
            occurrences = listOf(OccurrenceEntity(habitId = "h1", scheduledEpochDay = 19500, status = OccurrenceStatus.COMPLETED)),
            reminders = listOf(ReminderEntity(id = "r1", habitId = "h1", hour = 8, minute = 0, requestCode = 1)),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun testFutureVersionReturnsInvalid() {
        val payload = BackupData(
            version = 999,
            habits = emptyList(),
            occurrences = emptyList(),
            reminders = emptyList(),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testEmptyHabitNameReturnsInvalid() {
        val payload = BackupData(
            habits = listOf(HabitEntity(id = "h1", name = "")),
            occurrences = emptyList(),
            reminders = emptyList(),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testOrphanOccurrenceReturnsInvalid() {
        val payload = BackupData(
            habits = listOf(HabitEntity(id = "h1", name = "Đọc sách")),
            occurrences = listOf(OccurrenceEntity(habitId = "h_other", scheduledEpochDay = 19500, status = OccurrenceStatus.COMPLETED)),
            reminders = emptyList(),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testOrphanReminderReturnsInvalid() {
        val payload = BackupData(
            habits = listOf(HabitEntity(id = "h1", name = "Tập thể dục")),
            reminders = listOf(ReminderEntity(id = "r1", habitId = "h_non_existent", hour = 7, minute = 30, requestCode = 2)),
            occurrences = emptyList(),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testInvalidReminderHourReturnsInvalid() {
        val payload = BackupData(
            habits = listOf(HabitEntity(id = "h1", name = "Ngủ sớm")),
            reminders = listOf(ReminderEntity(id = "r1", habitId = "h1", hour = 25, minute = 70, requestCode = 3)),
            occurrences = emptyList(),
            goals = emptyList()
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is ValidationResult.Invalid)
    }
}
