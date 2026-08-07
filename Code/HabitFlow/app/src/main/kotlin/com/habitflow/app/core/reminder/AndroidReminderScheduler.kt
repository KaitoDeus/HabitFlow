package com.habitflow.app.core.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.habitflow.app.ReminderReceiver
import java.time.ZonedDateTime

class AndroidReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Đặt lịch báo thức nhắc nhở thói quen vào thời điểm (hour:minute) kế tiếp.
     */
    fun schedule(requestCode: Int, habitName: String, hour: Int, minute: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_name", habitName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = ZonedDateTime.now()
        var nextTargetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        
        // Nếu thời gian đã qua trong ngày hôm nay, lên lịch cho ngày mai
        if (!nextTargetTime.isAfter(now)) {
            nextTargetTime = nextTargetTime.plusDays(1)
        }

        alarmManager?.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTargetTime.toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    /**
     * Hủy lịch báo thức dựa theo requestCode.
     */
    fun cancel(requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pendingIntent)
    }
}
