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
    val occurrences: Flow<List<OccurrenceEntity>> = db.occurrenceDao().observeAll()
    val goals: Flow<List<GoalEntity>> = db.goalDao().observeActive()

    suspend fun addHabit(name: String, description: String = "", scheduledDays: String = "", scheduledTime: String? = null) {
        require(name.isNotBlank())
        db.habitDao().upsert(HabitEntity(UUID.randomUUID().toString(), name.trim(), description.trim(), scheduledDays = scheduledDays, scheduledTime = scheduledTime))
    }
    suspend fun archiveHabit(id: String) = db.habitDao().archive(id)
    suspend fun deleteHabit(id: String) = db.habitDao().delete(id)
    suspend fun mark(habitId: String, status: OccurrenceStatus, value: Double? = null) {
        db.occurrenceDao().upsert(OccurrenceEntity(habitId, LocalDate.now().toEpochDay(), status, value))
    }
    suspend fun unmark(habitId: String, dateEpochDay: Long) {
        db.occurrenceDao().delete(habitId, dateEpochDay)
    }
    suspend fun addGoal(name: String, target: Double, type: GoalMetricType) {
        require(name.isNotBlank() && target > 0)
        db.goalDao().upsert(GoalEntity(UUID.randomUUID().toString(), name.trim(), type, target,
            unit = if (type == GoalMetricType.OCCURRENCE_COUNT) "lần" else "đơn vị",
            startEpochDay = LocalDate.now().toEpochDay()))
    }
    suspend fun addGoalProgress(goal: GoalEntity, value: Double) {
        db.goalDao().upsert(goal.copy(currentValue = (goal.currentValue + value).coerceAtMost(goal.targetValue)))
    }

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = false }
    suspend fun exportJson(): String = json.encodeToString(BackupData(
        habits = db.habitDao().all(), occurrences = db.occurrenceDao().all(),
        goals = db.goalDao().all(), reminders = db.reminderDao().all()))

    suspend fun restoreJson(text: String) {
        val data = json.decodeFromString<BackupData>(text)
        require(data.version == 1) { "Phiên bản backup không được hỗ trợ" }
        db.withTransaction {
            db.reminderDao().clear(); db.goalDao().clear(); db.occurrenceDao().clear(); db.habitDao().clear()
            db.habitDao().upsertAll(data.habits)
            db.occurrenceDao().upsertAll(data.occurrences)
            db.goalDao().upsertAll(data.goals)
            db.reminderDao().upsertAll(data.reminders)
        }
    }
}
