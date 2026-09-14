package com.habitflow.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HabitFlowApplication : Application() {
    val database by lazy { HabitFlowDatabase.get(this) }
    val repository by lazy { HabitRepository(database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSource(this@HabitFlowApplication).userPreferencesStream.first()
                if (prefs.isAutoBackupEnabled) {
                    AutoBackupWorker.schedulePeriodic(this@HabitFlowApplication)
                }
            } catch (_: Exception) {}
        }
    }
}
