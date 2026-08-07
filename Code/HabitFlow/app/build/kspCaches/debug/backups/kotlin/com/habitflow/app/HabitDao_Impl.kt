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
public class HabitDao_Impl(
  __db: RoomDatabase,
) : HabitDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfHabitEntity: EntityUpsertAdapter<HabitEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfHabitEntity = EntityUpsertAdapter<HabitEntity>(object : EntityInsertAdapter<HabitEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `habits` (`id`,`name`,`description`,`unit`,`scheduledDays`,`scheduledTime`,`archived`,`createdAt`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HabitEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.unit)
        statement.bindText(5, entity.scheduledDays)
        val _tmpScheduledTime: String? = entity.scheduledTime
        if (_tmpScheduledTime == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpScheduledTime)
        }
        val _tmp: Int = if (entity.archived) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<HabitEntity>() {
      protected override fun createQuery(): String = "UPDATE `habits` SET `id` = ?,`name` = ?,`description` = ?,`unit` = ?,`scheduledDays` = ?,`scheduledTime` = ?,`archived` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HabitEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.unit)
        statement.bindText(5, entity.scheduledDays)
        val _tmpScheduledTime: String? = entity.scheduledTime
        if (_tmpScheduledTime == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpScheduledTime)
        }
        val _tmp: Int = if (entity.archived) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.createdAt)
        statement.bindText(9, entity.id)
      }
    })
  }

  public override suspend fun upsert(item: HabitEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfHabitEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<HabitEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfHabitEntity.upsert(_connection, items)
  }

  public override fun observeActive(): Flow<List<HabitEntity>> {
    val _sql: String = "SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("habits")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduledDays")
        val _columnIndexOfScheduledTime: Int = getColumnIndexOrThrow(_stmt, "scheduledTime")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<HabitEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HabitEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpScheduledDays: String
          _tmpScheduledDays = _stmt.getText(_columnIndexOfScheduledDays)
          val _tmpScheduledTime: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmpScheduledTime = null
          } else {
            _tmpScheduledTime = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = HabitEntity(_tmpId,_tmpName,_tmpDescription,_tmpUnit,_tmpScheduledDays,_tmpScheduledTime,_tmpArchived,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun all(): List<HabitEntity> {
    val _sql: String = "SELECT * FROM habits"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfUnit: Int = getColumnIndexOrThrow(_stmt, "unit")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduledDays")
        val _columnIndexOfScheduledTime: Int = getColumnIndexOrThrow(_stmt, "scheduledTime")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<HabitEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HabitEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpUnit: String
          _tmpUnit = _stmt.getText(_columnIndexOfUnit)
          val _tmpScheduledDays: String
          _tmpScheduledDays = _stmt.getText(_columnIndexOfScheduledDays)
          val _tmpScheduledTime: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmpScheduledTime = null
          } else {
            _tmpScheduledTime = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = HabitEntity(_tmpId,_tmpName,_tmpDescription,_tmpUnit,_tmpScheduledDays,_tmpScheduledTime,_tmpArchived,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun archive(id: String) {
    val _sql: String = "UPDATE habits SET archived = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM habits WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM habits"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
