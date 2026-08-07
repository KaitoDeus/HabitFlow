package com.habitflow.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.ZonedDateTime

object ReminderScheduler {
    fun schedule(context: Context, requestCode: Int, habitName: String, hour: Int, minute: Int) {
        com.habitflow.app.core.reminder.AndroidReminderScheduler(context).schedule(requestCode, habitName, hour, minute)
    }

    fun cancel(context: Context, requestCode: Int) {
        com.habitflow.app.core.reminder.AndroidReminderScheduler(context).cancel(requestCode)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("habit_name") ?: "thực hiện thói quen"
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = com.habitflow.app.core.reminder.NotificationFactory.createReminderNotification(context, habitName)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
