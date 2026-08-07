package com.habitflow.app

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReminderDao_Impl(
  __db: RoomDatabase,
) : ReminderDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfReminderEntity: EntityUpsertAdapter<ReminderEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfReminderEntity = EntityUpsertAdapter<ReminderEntity>(object : EntityInsertAdapter<ReminderEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `reminders` (`id`,`habitId`,`hour`,`minute`,`enabled`,`requestCode`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.habitId)
        statement.bindLong(3, entity.hour.toLong())
        statement.bindLong(4, entity.minute.toLong())
        val _tmp: Int = if (entity.enabled) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.requestCode.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<ReminderEntity>() {
      protected override fun createQuery(): String = "UPDATE `reminders` SET `id` = ?,`habitId` = ?,`hour` = ?,`minute` = ?,`enabled` = ?,`requestCode` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.habitId)
        statement.bindLong(3, entity.hour.toLong())
        statement.bindLong(4, entity.minute.toLong())
        val _tmp: Int = if (entity.enabled) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.requestCode.toLong())
        statement.bindText(7, entity.id)
      }
    })
  }

  public override suspend fun upsert(item: ReminderEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfReminderEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<ReminderEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfReminderEntity.upsert(_connection, items)
  }

  public override suspend fun all(): List<ReminderEntity> {
    val _sql: String = "SELECT * FROM reminders"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfHabitId: Int = getColumnIndexOrThrow(_stmt, "habitId")
        val _columnIndexOfHour: Int = getColumnIndexOrThrow(_stmt, "hour")
        val _columnIndexOfMinute: Int = getColumnIndexOrThrow(_stmt, "minute")
        val _columnIndexOfEnabled: Int = getColumnIndexOrThrow(_stmt, "enabled")
        val _columnIndexOfRequestCode: Int = getColumnIndexOrThrow(_stmt, "requestCode")
        val _result: MutableList<ReminderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ReminderEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpHabitId: String
          _tmpHabitId = _stmt.getText(_columnIndexOfHabitId)
          val _tmpHour: Int
          _tmpHour = _stmt.getLong(_columnIndexOfHour).toInt()
          val _tmpMinute: Int
          _tmpMinute = _stmt.getLong(_columnIndexOfMinute).toInt()
          val _tmpEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEnabled).toInt()
          _tmpEnabled = _tmp != 0
          val _tmpRequestCode: Int
          _tmpRequestCode = _stmt.getLong(_columnIndexOfRequestCode).toInt()
          _item = ReminderEntity(_tmpId,_tmpHabitId,_tmpHour,_tmpMinute,_tmpEnabled,_tmpRequestCode)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM reminders"
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
