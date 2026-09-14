package com.habitflow.app

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable enum class OccurrenceStatus { PENDING, COMPLETED, MISSED, SKIPPED, FROZEN }
@Serializable enum class GoalMetricType { OCCURRENCE_COUNT, ACCUMULATED_VALUE }
@Serializable enum class GoalPeriodType { WEEKLY,MONTHLY, CUSTOM
}
@Serializable
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val unit: String = "lần",
    val scheduledDays: String = "",
    val scheduledTime: String? = null,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(
    tableName = "occurrences",
    primaryKeys = ["habitId", "scheduledEpochDay"],
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("habitId")],
)
data class OccurrenceEntity(
    val habitId: String,
    val scheduledEpochDay: Long,
    val status: OccurrenceStatus,
    val completedValue: Double? = null,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: String = "current_user",
    val xp: Long = 0,
    val level: Int = 1,
    val streakFreezes: Int = 0,
    val skipsAvailable: Int = 0,
    val lastAwardedStreakFreezeEpochDay: Long = 0,
)

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val metricType: GoalMetricType,
    val periodType: GoalPeriodType = GoalPeriodType.WEEKLY,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String = "lần",
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val archived: Boolean = false,
    val linkedHabitId: String? = null,
    val contributionValue: Double = 1.0
)

@Serializable
@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("habitId")],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val requestCode: Int,
)

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val habits: List<HabitEntity>,
    val occurrences: List<OccurrenceEntity>,
    val goals: List<GoalEntity>,
    val reminders: List<ReminderEntity>,
    val userStats: UserStatsEntity? = null,
)

data class HabitStats(
    val completed: Int = 0,
    val missed: Int = 0,
    val skipped: Int = 0,
    val frozen: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Double = 0.0,
    val weeklyCompletionRate: Double = 0.0,
    val monthlyCompletionRate: Double = 0.0,
)
