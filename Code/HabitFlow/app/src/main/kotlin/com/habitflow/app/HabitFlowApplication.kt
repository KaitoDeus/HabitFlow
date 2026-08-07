package com.habitflow.app

import android.app.Application
import com.habitflow.app.core.reminder.ChannelManager

class HabitFlowApplication : Application() {
    val database by lazy { HabitFlowDatabase.get(this) }
    val repository by lazy { HabitRepository(database) }

    override fun onCreate() {
        super.onCreate()
        ChannelManager.createNotificationChannels(this)
    }
}

