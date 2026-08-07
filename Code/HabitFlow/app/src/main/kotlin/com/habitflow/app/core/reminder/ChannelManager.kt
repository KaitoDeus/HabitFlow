package com.habitflow.app.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ChannelManager {
    const val HABIT_REMINDER_CHANNEL_ID = "habit_reminders_channel"
    private const val CHANNEL_NAME = "Nhắc nhở thói quen"
    private const val CHANNEL_DESCRIPTION = "Kênh gửi thông báo nhắc nhở thực hiện thói quen hàng ngày"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HABIT_REMINDER_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
