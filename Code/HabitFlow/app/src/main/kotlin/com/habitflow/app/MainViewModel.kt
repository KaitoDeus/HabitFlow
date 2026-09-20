package com.habitflow.app

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitFlowApplication).repository
    private val sharedPrefs = application.getSharedPreferences("habitflow_test_prefs", Context.MODE_PRIVATE)

    private val _testDateOffset = MutableStateFlow(sharedPrefs.getLong("test_date_offset", 0L))
    val testDateOffset = _testDateOffset.asStateFlow()

    fun advanceTestDay() {
        val nextOffset = _testDateOffset.value + 1
        sharedPrefs.edit().putLong("test_date_offset", nextOffset).apply()
        _testDateOffset.value = nextOffset
        viewModelScope.launch { updateWidget() }
    }

    private suspend fun updateWidget() {
        // Wait a small buffer time for the Room database transaction to fully flush to disk
        delay(150)

        // 1. Trigger Glance update engine
        HabitWidget().updateAll(getApplication())

        // 2. Explicitly notify the system via generic package broadcast to refresh app widget provider states
        val context = getApplication<Application>().applicationContext
        val intent = Intent("android.appwidget.action.APPWIDGET_UPDATE").apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }

    val habits = repository.habits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val archivedHabits = repository.archivedHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun unarchiveHabit(id: String) = viewModelScope.launch {
        repository.unarchiveHabit(id)
        updateWidget()
    }
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val occurrences = repository.occurrences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val stats = combine(repository.occurrences, repository.habits, testDateOffset) { occurrences, habits, offset ->
        val todayEpochDay = LocalDate.now().plusDays(offset).toEpochDay()
        HabitStatisticsCalculator.calculate(occurrences, habits, todayEpochDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitStats())

    fun addHabit(name: String, description: String = "", scheduledDays: String = "", scheduledTime: String? = null) = viewModelScope.launch {
        val habitId = repository.addHabit(name, description, scheduledDays, scheduledTime)
        if (!scheduledTime.isNullOrBlank()) {
            val parts = scheduledTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour != null && minute != null) {
                    val reminder = (getApplication() as HabitFlowApplication).database.reminderDao().all().find { it.habitId == habitId }
                    if (reminder != null) {
                        ReminderScheduler.schedule(getApplication(), reminder, name.trim())
                    }
                }
            }
        }
        updateWidget()
    }
    fun archiveHabit(id: String) = viewModelScope.launch {
        repository.archiveHabit(id)
        updateWidget()
    }
    fun deleteHabit(id: String) = viewModelScope.launch {
        val reminder = (getApplication() as HabitFlowApplication).database.reminderDao().all().find { it.habitId == id }
        if (reminder != null) {
            ReminderScheduler.cancel(getApplication(), reminder)
        }
        repository.deleteHabit(id)
        updateWidget()
    }

    fun mark(id: String, status: OccurrenceStatus) = viewModelScope.launch {
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        repository.mark(id, status, dateEpochDay = todayEpochDay)
        if (status == OccurrenceStatus.COMPLETED) {
            val habitStats = stats.value
            GamificationManager.processCompletion(repository, habitStats.currentStreak, todayEpochDay)

            // Auto-progress linked goals
            goals.value.filter { it.linkedHabitId == id }.forEach { goal ->
                repository.addGoalProgress(goal, goal.contributionValue, todayEpochDay)
            }
        }
        updateWidget()
    }

    fun useStreakFreeze(habitId: String) = viewModelScope.launch {
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        if (GamificationManager.useStreakFreeze(repository)) {
            repository.mark(habitId, OccurrenceStatus.FROZEN, dateEpochDay = todayEpochDay)
            updateWidget()
        }
    }

    fun useSkip(habitId: String) = viewModelScope.launch {
        val todayEpochDay = LocalDate.now().plusDays(_testDateOffset.value).toEpochDay()
        if (GamificationManager.useSkipCard(repository)) {
            repository.mark(habitId, OccurrenceStatus.SKIPPED, dateEpochDay = todayEpochDay)
            updateWidget()
        }
    }

    fun skipLevel() = viewModelScope.launch {
        GamificationManager.skipLevel(repository)
        updateWidget()
    }

    fun unmark(id: String, dateEpochDay: Long) = viewModelScope.launch {
        val currentOccurrences = repository.getOccurrencesDirect()
        val existing = currentOccurrences.find { it.habitId == id && it.scheduledEpochDay == dateEpochDay }
        if (existing?.status == OccurrenceStatus.COMPLETED) {
            val habitStats = stats.value
            GamificationManager.processReset(repository, habitStats.currentStreak, dateEpochDay)

            // Undo goal progress
            goals.value.filter { it.linkedHabitId == id }.forEach { goal ->
                repository.addGoalProgress(goal, -goal.contributionValue, dateEpochDay)
            }
        }
        repository.unmark(id, dateEpochDay)
        updateWidget()
    }
    fun addGoal(
        name: String,
        target: Double,
        type: GoalMetricType,
        periodType: GoalPeriodType = GoalPeriodType.CUSTOM,
        unit: String = "lần",
        startEpochDay: Long = LocalDate.now().toEpochDay(),
        endEpochDay: Long? = null,
        linkedHabitId: String? = null,
        contributionValue: Double = 1.0
    ) = viewModelScope.launch {
        repository.addGoal(name, target, type, periodType, unit, startEpochDay, endEpochDay, linkedHabitId, contributionValue)
        updateWidget()
    }
    fun deleteGoal(id: String) = viewModelScope.launch {
        repository.deleteGoal(id)
        updateWidget()
    }
    fun updateGoal(goal: GoalEntity) = viewModelScope.launch {
        repository.updateGoal(goal)
        updateWidget()
    }
    fun addGoalProgress(goal: GoalEntity, value: Double) = viewModelScope.launch {
        val currentEpochDay = java.time.LocalDate.now()
            .plusDays(testDateOffset.value)
            .toEpochDay()

        val success = repository.addGoalProgress(goal, value, currentEpochDay)
        if (success) {
            updateWidget()
        }
    }

    suspend fun exportJson(): String = repository.exportJson()
    suspend fun restoreJson(text: String) = repository.restoreJson(text)
}
