package com.habitflow.app

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class OccurrenceDao_Impl(
  __db: RoomDatabase,
) : OccurrenceDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfOccurrenceEntity: EntityUpsertAdapter<OccurrenceEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfOccurrenceEntity = EntityUpsertAdapter<OccurrenceEntity>(object : EntityInsertAdapter<OccurrenceEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `occurrences` (`habitId`,`scheduledEpochDay`,`status`,`completedValue`,`note`,`updatedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: OccurrenceEntity) {
        statement.bindText(1, entity.habitId)
        statement.bindLong(2, entity.scheduledEpochDay)
        statement.bindText(3, __OccurrenceStatus_enumToString(entity.status))
        val _tmpCompletedValue: Double? = entity.completedValue
        if (_tmpCompletedValue == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpCompletedValue)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNote)
        }
        statement.bindLong(6, entity.updatedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<OccurrenceEntity>() {
      protected override fun createQuery(): String = "UPDATE `occurrences` SET `habitId` = ?,`scheduledEpochDay` = ?,`status` = ?,`completedValue` = ?,`note` = ?,`updatedAt` = ? WHERE `habitId` = ? AND `scheduledEpochDay` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: OccurrenceEntity) {
        statement.bindText(1, entity.habitId)
        statement.bindLong(2, entity.scheduledEpochDay)
        statement.bindText(3, __OccurrenceStatus_enumToString(entity.status))
        val _tmpCompletedValue: Double? = entity.completedValue
        if (_tmpCompletedValue == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpCompletedValue)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNote)
        }
        statement.bindLong(6, entity.updatedAt)
        statement.bindText(7, entity.habitId)
        statement.bindLong(8, entity.scheduledEpochDay)
      }
    })
  }

  public override suspend fun upsert(item: OccurrenceEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfOccurrenceEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<OccurrenceEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfOccurrenceEntity.upsert(_connection, items)
  }

  public override fun observeAll(): Flow<List<OccurrenceEntity>> {
    val _sql: String = "SELECT * FROM occurrences"
    return createFlow(__db, false, arrayOf("occurrences")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfHabitId: Int = getColumnIndexOrThrow(_stmt, "habitId")
        val _columnIndexOfScheduledEpochDay: Int = getColumnIndexOrThrow(_stmt, "scheduledEpochDay")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCompletedValue: Int = getColumnIndexOrThrow(_stmt, "completedValue")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<OccurrenceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: OccurrenceEntity
          val _tmpHabitId: String
          _tmpHabitId = _stmt.getText(_columnIndexOfHabitId)
          val _tmpScheduledEpochDay: Long
          _tmpScheduledEpochDay = _stmt.getLong(_columnIndexOfScheduledEpochDay)
          val _tmpStatus: OccurrenceStatus
          _tmpStatus = __OccurrenceStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpCompletedValue: Double?
          if (_stmt.isNull(_columnIndexOfCompletedValue)) {
            _tmpCompletedValue = null
          } else {
            _tmpCompletedValue = _stmt.getDouble(_columnIndexOfCompletedValue)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = OccurrenceEntity(_tmpHabitId,_tmpScheduledEpochDay,_tmpStatus,_tmpCompletedValue,_tmpNote,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun all(): List<OccurrenceEntity> {
    val _sql: String = "SELECT * FROM occurrences"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfHabitId: Int = getColumnIndexOrThrow(_stmt, "habitId")
        val _columnIndexOfScheduledEpochDay: Int = getColumnIndexOrThrow(_stmt, "scheduledEpochDay")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCompletedValue: Int = getColumnIndexOrThrow(_stmt, "completedValue")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<OccurrenceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: OccurrenceEntity
          val _tmpHabitId: String
          _tmpHabitId = _stmt.getText(_columnIndexOfHabitId)
          val _tmpScheduledEpochDay: Long
          _tmpScheduledEpochDay = _stmt.getLong(_columnIndexOfScheduledEpochDay)
          val _tmpStatus: OccurrenceStatus
          _tmpStatus = __OccurrenceStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpCompletedValue: Double?
          if (_stmt.isNull(_columnIndexOfCompletedValue)) {
            _tmpCompletedValue = null
          } else {
            _tmpCompletedValue = _stmt.getDouble(_columnIndexOfCompletedValue)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = OccurrenceEntity(_tmpHabitId,_tmpScheduledEpochDay,_tmpStatus,_tmpCompletedValue,_tmpNote,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(habitId: String, dateEpochDay: Long) {
    val _sql: String = "DELETE FROM occurrences WHERE habitId = ? AND scheduledEpochDay = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, habitId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, dateEpochDay)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM occurrences"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __OccurrenceStatus_enumToString(_value: OccurrenceStatus): String = when (_value) {
    OccurrenceStatus.PENDING -> "PENDING"
    OccurrenceStatus.COMPLETED -> "COMPLETED"
    OccurrenceStatus.MISSED -> "MISSED"
    OccurrenceStatus.SKIPPED -> "SKIPPED"
  }

  private fun __OccurrenceStatus_stringToEnum(_value: String): OccurrenceStatus = when (_value) {
    "PENDING" -> OccurrenceStatus.PENDING
    "COMPLETED" -> OccurrenceStatus.COMPLETED
    "MISSED" -> OccurrenceStatus.MISSED
    "SKIPPED" -> OccurrenceStatus.SKIPPED
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
