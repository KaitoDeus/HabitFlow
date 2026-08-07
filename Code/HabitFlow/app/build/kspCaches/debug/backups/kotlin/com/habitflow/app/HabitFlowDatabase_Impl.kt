package com.habitflow.app

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HabitFlowDatabase_Impl : HabitFlowDatabase() {
  private val _habitDao: Lazy<HabitDao> = lazy {
    HabitDao_Impl(this)
  }

  private val _occurrenceDao: Lazy<OccurrenceDao> = lazy {
    OccurrenceDao_Impl(this)
  }

  private val _goalDao: Lazy<GoalDao> = lazy {
    GoalDao_Impl(this)
  }

  private val _reminderDao: Lazy<ReminderDao> = lazy {
    ReminderDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "252f181c6474dd927b383282fa9002e9", "8b3670d1afc09f972d75878420160aaf") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `habits` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `unit` TEXT NOT NULL, `scheduledDays` TEXT NOT NULL, `scheduledTime` TEXT, `archived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `occurrences` (`habitId` TEXT NOT NULL, `scheduledEpochDay` INTEGER NOT NULL, `status` TEXT NOT NULL, `completedValue` REAL, `note` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`habitId`, `scheduledEpochDay`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_occurrences_habitId` ON `occurrences` (`habitId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `metricType` TEXT NOT NULL, `targetValue` REAL NOT NULL, `currentValue` REAL NOT NULL, `unit` TEXT NOT NULL, `startEpochDay` INTEGER NOT NULL, `endEpochDay` INTEGER, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` TEXT NOT NULL, `habitId` TEXT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `requestCode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_habitId` ON `reminders` (`habitId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '252f181c6474dd927b383282fa9002e9')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `habits`")
        connection.execSQL("DROP TABLE IF EXISTS `occurrences`")
        connection.execSQL("DROP TABLE IF EXISTS `goals`")
        connection.execSQL("DROP TABLE IF EXISTS `reminders`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsHabits: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHabits.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("scheduledDays", TableInfo.Column("scheduledDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("scheduledTime", TableInfo.Column("scheduledTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabits.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHabits: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHabits: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoHabits: TableInfo = TableInfo("habits", _columnsHabits, _foreignKeysHabits, _indicesHabits)
        val _existingHabits: TableInfo = read(connection, "habits")
        if (!_infoHabits.equals(_existingHabits)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |habits(com.habitflow.app.HabitEntity).
              | Expected:
              |""".trimMargin() + _infoHabits + """
              |
              | Found:
              |""".trimMargin() + _existingHabits)
        }
        val _columnsOccurrences: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOccurrences.put("habitId", TableInfo.Column("habitId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOccurrences.put("scheduledEpochDay", TableInfo.Column("scheduledEpochDay", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOccurrences.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOccurrences.put("completedValue", TableInfo.Column("completedValue", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOccurrences.put("note", TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOccurrences.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOccurrences: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysOccurrences.add(TableInfo.ForeignKey("habits", "CASCADE", "NO ACTION", listOf("habitId"), listOf("id")))
        val _indicesOccurrences: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesOccurrences.add(TableInfo.Index("index_occurrences_habitId", false, listOf("habitId"), listOf("ASC")))
        val _infoOccurrences: TableInfo = TableInfo("occurrences", _columnsOccurrences, _foreignKeysOccurrences, _indicesOccurrences)
        val _existingOccurrences: TableInfo = read(connection, "occurrences")
        if (!_infoOccurrences.equals(_existingOccurrences)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |occurrences(com.habitflow.app.OccurrenceEntity).
              | Expected:
              |""".trimMargin() + _infoOccurrences + """
              |
              | Found:
              |""".trimMargin() + _existingOccurrences)
        }
        val _columnsGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGoals.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("metricType", TableInfo.Column("metricType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("targetValue", TableInfo.Column("targetValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("currentValue", TableInfo.Column("currentValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("unit", TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("startEpochDay", TableInfo.Column("startEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("endEpochDay", TableInfo.Column("endEpochDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGoals: TableInfo = TableInfo("goals", _columnsGoals, _foreignKeysGoals, _indicesGoals)
        val _existingGoals: TableInfo = read(connection, "goals")
        if (!_infoGoals.equals(_existingGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |goals(com.habitflow.app.GoalEntity).
              | Expected:
              |""".trimMargin() + _infoGoals + """
              |
              | Found:
              |""".trimMargin() + _existingGoals)
        }
        val _columnsReminders: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReminders.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("habitId", TableInfo.Column("habitId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("hour", TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("minute", TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("enabled", TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminders.put("requestCode", TableInfo.Column("requestCode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReminders: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysReminders.add(TableInfo.ForeignKey("habits", "CASCADE", "NO ACTION", listOf("habitId"), listOf("id")))
        val _indicesReminders: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesReminders.add(TableInfo.Index("index_reminders_habitId", false, listOf("habitId"), listOf("ASC")))
        val _infoReminders: TableInfo = TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders)
        val _existingReminders: TableInfo = read(connection, "reminders")
        if (!_infoReminders.equals(_existingReminders)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reminders(com.habitflow.app.ReminderEntity).
              | Expected:
              |""".trimMargin() + _infoReminders + """
              |
              | Found:
              |""".trimMargin() + _existingReminders)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "habits", "occurrences", "goals", "reminders")
  }

  public override fun clearAllTables() {
    super.performClear(true, "habits", "occurrences", "goals", "reminders")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(HabitDao::class, HabitDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(OccurrenceDao::class, OccurrenceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GoalDao::class, GoalDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ReminderDao::class, ReminderDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun habitDao(): HabitDao = _habitDao.value

  public override fun occurrenceDao(): OccurrenceDao = _occurrenceDao.value

  public override fun goalDao(): GoalDao = _goalDao.value

  public override fun reminderDao(): ReminderDao = _reminderDao.value
}
