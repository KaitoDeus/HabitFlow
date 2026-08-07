package com.habitflow.app

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<HabitEntity>>
    @Query("SELECT * FROM habits") suspend fun all(): List<HabitEntity>
    @Upsert suspend fun upsert(item: HabitEntity)
    @Upsert suspend fun upsertAll(items: List<HabitEntity>)
    @Query("UPDATE habits SET archived = 1 WHERE id = :id") suspend fun archive(id: String)
    @Query("DELETE FROM habits WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM habits") suspend fun clear()
}

@Dao
interface OccurrenceDao {
    @Query("SELECT * FROM occurrences") fun observeAll(): Flow<List<OccurrenceEntity>>
    @Query("SELECT * FROM occurrences") suspend fun all(): List<OccurrenceEntity>
    @Upsert suspend fun upsert(item: OccurrenceEntity)
    @Upsert suspend fun upsertAll(items: List<OccurrenceEntity>)
    @Query("DELETE FROM occurrences WHERE habitId = :habitId AND scheduledEpochDay = :dateEpochDay")
    suspend fun delete(habitId: String, dateEpochDay: Long)
    @Query("DELETE FROM occurrences") suspend fun clear()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE archived = 0 ORDER BY startEpochDay DESC")
    fun observeActive(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals") suspend fun all(): List<GoalEntity>
    @Upsert suspend fun upsert(item: GoalEntity)
    @Upsert suspend fun upsertAll(items: List<GoalEntity>)
    @Query("DELETE FROM goals") suspend fun clear()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders") fun observeAll(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders") suspend fun all(): List<ReminderEntity>
    @Query("SELECT * FROM reminders WHERE habitId = :habitId") suspend fun getByHabitId(habitId: String): ReminderEntity?
    @Upsert suspend fun upsert(item: ReminderEntity)
    @Upsert suspend fun upsertAll(items: List<ReminderEntity>)
    @Query("DELETE FROM reminders WHERE habitId = :habitId") suspend fun deleteByHabitId(habitId: String)
    @Query("DELETE FROM reminders") suspend fun clear()
}

@Database(
    entities = [HabitEntity::class, OccurrenceEntity::class, GoalEntity::class, ReminderEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class HabitFlowDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: HabitFlowDatabase? = null
        fun get(context: Context): HabitFlowDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                HabitFlowDatabase::class.java,
                "habitflow.db",
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
