package com.habitflow.app

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

class HabitRepository(private val db: HabitFlowDatabase) {
    val habits: Flow<List<HabitEntity>> = db.habitDao().observeActive()
    val archivedHabits: Flow<List<HabitEntity>> = db.habitDao().observeArchived()
    val occurrences: Flow<List<OccurrenceEntity>> = db.occurrenceDao().observeAll()
    val goals: Flow<List<GoalEntity>> = db.goalDao().observeActive()
    val userStats: Flow<UserStatsEntity?> = db.userStatsDao().observe()

    suspend fun addHabit(name: String, description: String = "", scheduledDays: String = "", scheduledTime: String? = null): String {
        require(name.isNotBlank())
        val habitId = UUID.randomUUID().toString()
        db.habitDao().upsert(HabitEntity(habitId, name.trim(), description.trim(), scheduledDays = scheduledDays, scheduledTime = scheduledTime))
        if (!scheduledTime.isNullOrBlank()) {
            val parts = scheduledTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour != null && minute != null) {
                    val reminder = ReminderEntity(
                        id = UUID.randomUUID().toString(),
                        habitId = habitId,
                        hour = hour,
                        minute = minute,
                        enabled = true,
                        requestCode = (System.currentTimeMillis() % 100000).toInt()
                    )
                    db.reminderDao().upsert(reminder)
                }
            }
        }
        return habitId
    }
    suspend fun archiveHabit(id: String) = db.habitDao().archive(id)
    suspend fun unarchiveHabit(id: String) = db.habitDao().unarchive(id)
    suspend fun deleteHabit(id: String) = db.habitDao().delete(id)
    suspend fun mark(habitId: String, status: OccurrenceStatus, value: Double? = null, dateEpochDay: Long = LocalDate.now().toEpochDay()) {
        db.occurrenceDao().upsert(OccurrenceEntity(habitId, dateEpochDay, status, value))
    }
    suspend fun unmark(habitId: String, dateEpochDay: Long) {
        db.occurrenceDao().delete(habitId, dateEpochDay)
    }

    suspend fun getUserStats(): UserStatsEntity = db.userStatsDao().get() ?: UserStatsEntity().also { db.userStatsDao().upsert(it) }
    suspend fun updateUserStats(stats: UserStatsEntity) = db.userStatsDao().upsert(stats)
    suspend fun addGoal(
        name: String,
        target: Double,
        type: GoalMetricType,
        periodType: GoalPeriodType = GoalPeriodType.WEEKLY,
        unit: String = "lần",
        startEpochDay: Long = LocalDate.now().toEpochDay(),
        endEpochDay: Long? = null,
        linkedHabitId: String? = null,
        contributionValue: Double = 1.0
    ) {
        require(name.isNotBlank() && target > 0)
        db.goalDao().upsert(
            GoalEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                metricType = type,
                periodType = periodType,
                targetValue = target,
                currentValue = 0.0,
                unit = unit,
                startEpochDay = startEpochDay,
                endEpochDay = endEpochDay,
                archived = false,
                linkedHabitId = linkedHabitId,
                contributionValue = contributionValue
            )
        )
    }

    suspend fun updateGoal(goal: GoalEntity) {
        db.goalDao().upsert(goal)
    }
    suspend fun deleteGoal(id: String) = db.goalDao().delete(id)
    suspend fun addGoalProgress(
        goal: GoalEntity,
        value: Double,
        currentEpochDay: Long = LocalDate.now().toEpochDay() // Thêm giá trị mặc định vào đây
    ): Boolean {
        if (value > 0 && goal.lastUpdatedEpochDay == currentEpochDay) {
            return false
        }

        val updatedValue = (goal.currentValue + value).coerceIn(0.0, goal.targetValue)
        val updatedLastDay = if (value > 0) currentEpochDay else null

        db.goalDao().upsert(
            goal.copy(
                currentValue = updatedValue,
                lastUpdatedEpochDay = updatedLastDay
            )
        )
        return true
    }
    suspend fun getActiveHabitsDirect(): List<HabitEntity> = db.habitDao().all().filter { !it.archived }
    suspend fun getOccurrencesDirect(): List<OccurrenceEntity> = db.occurrenceDao().all()

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = false }
    suspend fun exportJson(): String = json.encodeToString(BackupData(
        habits = db.habitDao().all(), occurrences = db.occurrenceDao().all(),
        goals = db.goalDao().all(), reminders = db.reminderDao().all(),
        userStats = db.userStatsDao().get()))

    suspend fun restoreJson(text: String) {
        val data = json.decodeFromString<BackupData>(text)
        db.withTransaction {
            db.userStatsDao().clear(); db.reminderDao().clear(); db.goalDao().clear(); db.occurrenceDao().clear(); db.habitDao().clear()
            db.habitDao().upsertAll(data.habits)
            db.occurrenceDao().upsertAll(data.occurrences)
            db.goalDao().upsertAll(data.goals)
            db.reminderDao().upsertAll(data.reminders)
            data.userStats?.let { db.userStatsDao().upsert(it) }
        }
    }
}