package com.habitflow.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitFlowApplication).repository

    val habits = repository.habits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val occurrences = repository.occurrences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val stats = repository.occurrences.combine(repository.habits) { items, _ ->
        HabitStatisticsCalculator.calculate(items)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitStats())

    fun addHabit(name: String, description: String = "", scheduledDays: String = "", scheduledTime: String? = null) = viewModelScope.launch { repository.addHabit(name, description, scheduledDays, scheduledTime) }
    fun archiveHabit(id: String) = viewModelScope.launch { repository.archiveHabit(id) }
    fun deleteHabit(id: String) = viewModelScope.launch { repository.deleteHabit(id) }
    fun mark(id: String, status: OccurrenceStatus) = viewModelScope.launch { repository.mark(id, status) }
    fun unmark(id: String, dateEpochDay: Long) = viewModelScope.launch { repository.unmark(id, dateEpochDay) }
    fun addGoal(name: String, target: Double, type: GoalMetricType) = viewModelScope.launch { repository.addGoal(name, target, type) }
    fun addGoalProgress(goal: GoalEntity, value: Double) = viewModelScope.launch { repository.addGoalProgress(goal, value) }
    suspend fun exportJson(): String = repository.exportJson()
    suspend fun restoreJson(text: String) = repository.restoreJson(text)
}
