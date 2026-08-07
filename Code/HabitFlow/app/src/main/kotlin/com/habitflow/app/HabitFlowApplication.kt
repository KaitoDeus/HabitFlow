package com.habitflow.app

import android.app.Application

class HabitFlowApplication : Application() {
    val database by lazy { HabitFlowDatabase.get(this) }
    val repository by lazy { HabitRepository(database) }
}
