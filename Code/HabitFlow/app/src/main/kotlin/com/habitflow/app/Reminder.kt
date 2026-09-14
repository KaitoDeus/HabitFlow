package com.habitflow.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

const val ACTION_COMPLETE_HABIT = "com.habitflow.app.ACTION_COMPLETE_HABIT"
const val ACTION_SNOOZE_HABIT = "com.habitflow.app.ACTION_SNOOZE_HABIT"

// 1. QUẢN LÝ THÔNG BÁO (NOTIFICATION HELPER)
object NotificationHelper {
    const val CHANNEL_ID = "habit_reminder_channel"
    const val CHANNEL_NAME = "Nhắc nhở thói quen"
    const val CHANNEL_DESC = "Thông báo nhắc nhở thực hiện thói quen hàng ngày"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun createReminderNotification(
        context: Context,
        notificationId: Int,
        habitId: String = "",
        habitName: String,
        note: String? = null,
        vibrate: Boolean = true
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COMPLETE_HABIT
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_HABIT
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(AlarmReceiver.EXTRA_NOTE, note)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Nhắc nhở: $habitName")
            .setContentText(if (!note.isNullOrBlank()) note else "Đã đến giờ thực hiện thói quen của bạn!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (vibrate) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            builder.setVibrate(longArrayOf(0L))
        }

        if (habitId.isNotBlank()) {
            builder.addAction(0, "✓ Hoàn thành", completePendingIntent)
        }
        builder.addAction(0, "⏱ Hoãn 10 phút", snoozePendingIntent)

        return builder.build()
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        habitId: String = "",
        habitName: String,
        note: String? = null,
        vibrate: Boolean = true
    ) {
        val notification = createReminderNotification(context, notificationId, habitId, habitName, note, vibrate)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(notificationId, notification)
    }
}

// 2. BỘ ĐIỀU PHỐI BÁO THỨC (REMINDER SCHEDULER)
object ReminderScheduler {
    fun schedule(
        context: Context,
        reminder: ReminderEntity,
        habitName: String,
        note: String = ""
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, reminder.requestCode)
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, reminder.habitId)
            putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(AlarmReceiver.EXTRA_NOTE, note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

// 3. CÁC BROADCAST RECEIVERS HỆ THỐNG
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_NOTE = "extra_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1)
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: ""
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Thói quen hàng ngày"
        val note = intent.getStringExtra(EXTRA_NOTE)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSource(context.applicationContext).userPreferencesStream.first()
                if (prefs.isNotificationEnabled) {
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = notificationId,
                        habitId = habitId,
                        habitName = habitName,
                        note = note,
                        vibrate = prefs.isReminderVibrateEnabled
                    )
                }
            } catch (_: Exception) {
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = notificationId,
                    habitId = habitId,
                    habitName = habitName,
                    note = note
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, 0)
        val habitId = intent.getStringExtra(AlarmReceiver.EXTRA_HABIT_ID) ?: ""
        val habitName = intent.getStringExtra(AlarmReceiver.EXTRA_HABIT_NAME) ?: "Thói quen hàng ngày"
        val note = intent.getStringExtra(AlarmReceiver.EXTRA_NOTE)

        // Đóng notification ngay lập tức
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (notificationId != 0) {
            notificationManager?.cancel(notificationId)
        }

        when (intent.action) {
            ACTION_COMPLETE_HABIT -> {
                if (habitId.isNotBlank()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val app = context.applicationContext as HabitFlowApplication
                            val repo = app.repository
                            val db = app.database
                            val today = java.time.LocalDate.now()
                            val todayEpochDay = today.toEpochDay()

                            repo.mark(habitId, OccurrenceStatus.COMPLETED, dateEpochDay = todayEpochDay)

                            val habits = repo.getActiveHabitsDirect()
                            val occurrences = repo.getOccurrencesDirect()
                            val stats = HabitStatisticsCalculator.calculate(occurrences, habits, todayEpochDay)
                            GamificationManager.processCompletion(repo, stats.currentStreak, todayEpochDay)

                            val goals = db.goalDao().all()
                            goals.filter { it.linkedHabitId == habitId }.forEach { goal ->
                                repo.addGoalProgress(goal, goal.contributionValue)
                            }

                            try {
                                HabitWidget().updateAll(context.applicationContext)
                                val widgetIntent = Intent("android.appwidget.action.APPWIDGET_UPDATE").apply {
                                    `package` = context.packageName
                                }
                                context.sendBroadcast(widgetIntent)
                            } catch (_: Exception) {}
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_SNOOZE_HABIT -> {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                        putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
                        putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName)
                        putExtra(AlarmReceiver.EXTRA_NOTE, note)
                    }
                    val snoozePendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId + 100000,
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000L // 10 phút sau
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    triggerTime,
                                    snoozePendingIntent
                                )
                            } else {
                                alarmManager.setAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    triggerTime,
                                    snoozePendingIntent
                                )
                            }
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                snoozePendingIntent
                            )
                        }
                    } catch (_: Exception) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            snoozePendingIntent
                        )
                    }
                }
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HabitFlowDatabase.get(context)
                    val activeReminders = db.reminderDao().all().filter { it.enabled }
                    val habits = db.habitDao().all().associateBy { it.id }

                    activeReminders.forEach { reminder ->
                        val habit = habits[reminder.habitId]
                        val habitName = habit?.name ?: "Thói quen hàng ngày"
                        ReminderScheduler.schedule(context, reminder, habitName)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HabitFlowDatabase.get(context)
                    val activeReminders = db.reminderDao().all().filter { it.enabled }
                    val habits = db.habitDao().all().associateBy { it.id }

                    activeReminders.forEach { reminder ->
                        val habit = habits[reminder.habitId]
                        val habitName = habit?.name ?: "Thói quen hàng ngày"
                        ReminderScheduler.schedule(context, reminder, habitName)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
