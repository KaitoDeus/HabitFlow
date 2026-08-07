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
import kotlin.Boolean
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
public class GoalDao_Impl(
  __db: RoomDatabase,
) : GoalDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfGoalEntity: EntityUpsertAdapter<GoalEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfGoalEntity = EntityUpsertAdapter<GoalEntity>(object : EntityInsertAdapter<GoalEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `goals` (`id`,`name`,`metricType`,`targetValue`,`currentValue`,`unit`,`startEpochDay`,`endEpochDay`,`archived`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, __GoalMetricType_enumToString(entity.metricType))
        statement.bindDouble(4, entity.targetValue)
        statement.bindDouble(5, entity.currentValue)
        statement.bindText(6, entity.unit)
        statement.bindLong(7, entity.startEpochDay)
        val _tmpEndEpochDay: Long? = entity.endEpochDay
        if (_tmpEndEpochDay == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEndEpochDay)
        }
        val _tmp: Int = if (entity.archived) 1 else 0
        statement.bindLong(9, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<GoalEntity>() {
      protected override fun createQuery(): String = "UPDATE `goals` SET `id` = ?,`name` = ?,`metricType` = ?,`targetValue` = ?,`currentValue` = ?,`unit` = ?,`startEpochDay` = ?,`endEpochDay` = ?,`archived` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, __GoalMetricType_enumToString(entity.metricType))
        statement.bindDouble(4, entity.targetValue)
        statement.bindDouble(5, entity.currentValue)
        statement.bindText(6, entity.unit)
        statement.bindLong(7, entity.startEpochDay)
        val _tmpEndEpochDay: Long? = entity.endEpochDay
        if (_tmpEndEpochDay == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEndEpochDay)
        }
        val _tmp: Int = if (entity.archived) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindText(10, entity.id)
      }
    })
  }

  public override suspend fun upsert(item: GoalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfGoalEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<GoalEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfGoalEntity.upsert(_connection, items)
  }

  public override fun observeActive(): Flow<List<GoalEntity>> {
    val _sql: String = "SELECT * FROM goals WHERE archived = 0 ORDER BY startEpochDay DESC"
    return createFlow(__db, false, arrayOf("goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfStartEpochDay: Int = getColumnIndexOrThrow(_stmt, "startEpochDay")
        val _columnIndexOfEndEpochDay: Int = getColumnIndexOrThrow(_stmt, "endEpochDay")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMetricType: GoalMetricType
          _tmpMetricType = __GoalMetricType_stringToEnum(_stmt.getText(_columnIndexOfMetricType))
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpStartEpochDay: Long
          _tmpStartEpochDay = _stmt.getLong(_columnIndexOfStartEpochDay)
          val _tmpEndEpochDay: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochDay)) {
            _tmpEndEpochDay = null
          } else {
            _tmpEndEpochDay = _stmt.getLong(_columnIndexOfEndEpochDay)
          }
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          _item = GoalEntity(_tmpId,_tmpName,_tmpMetricType,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpStartEpochDay,_tmpEndEpochDay,_tmpArchived)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun all(): List<GoalEntity> {
    val _sql: String = "SELECT * FROM goals"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _columnIndexOfTargetValue: Int = getColumnIndexOrThrow(_stmt, "targetValue")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfStartEpochDay: Int = getColumnIndexOrThrow(_stmt, "startEpochDay")
        val _columnIndexOfEndEpochDay: Int = getColumnIndexOrThrow(_stmt, "endEpochDay")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMetricType: GoalMetricType
          _tmpMetricType = __GoalMetricType_stringToEnum(_stmt.getText(_columnIndexOfMetricType))
          val _tmpTargetValue: Double
          _tmpTargetValue = _stmt.getDouble(_columnIndexOfTargetValue)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpStartEpochDay: Long
          _tmpStartEpochDay = _stmt.getLong(_columnIndexOfStartEpochDay)
          val _tmpEndEpochDay: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochDay)) {
            _tmpEndEpochDay = null
          } else {
            _tmpEndEpochDay = _stmt.getLong(_columnIndexOfEndEpochDay)
          }
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          _item = GoalEntity(_tmpId,_tmpName,_tmpMetricType,_tmpTargetValue,_tmpCurrentValue,_tmpUnit,_tmpStartEpochDay,_tmpEndEpochDay,_tmpArchived)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM goals"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __GoalMetricType_enumToString(_value: GoalMetricType): String = when (_value) {
    GoalMetricType.OCCURRENCE_COUNT -> "OCCURRENCE_COUNT"
    GoalMetricType.ACCUMULATED_VALUE -> "ACCUMULATED_VALUE"
  }

  private fun __GoalMetricType_stringToEnum(_value: String): GoalMetricType = when (_value) {
    "OCCURRENCE_COUNT" -> GoalMetricType.OCCURRENCE_COUNT
    "ACCUMULATED_VALUE" -> GoalMetricType.ACCUMULATED_VALUE
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
