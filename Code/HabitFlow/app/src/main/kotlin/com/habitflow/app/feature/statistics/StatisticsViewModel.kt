package com.habitflow.app.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabitRepository
import com.habitflow.app.core.domain.calculator.StatisticsAggregator
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        observeStatistics()
    }

    private fun observeStatistics() {
        viewModelScope.launch {
            repository.occurrences.collect { occurrences ->

                try {
                    val today = LocalDate.now().toEpochDay()

                    val stats = StatisticsAggregator.calculate(
                        items = occurrences,
                        todayEpochDay = today
                    )

                    _uiState.value = StatisticsUiState(
                        completed = stats.completed,
                        missed = stats.missed,
                        skipped = stats.skipped,
                        frozen = stats.frozen,
                        currentStreak = stats.currentStreak,
                        longestStreak = stats.longestStreak,
                        completionRate = stats.completionRate,
                        weeklyCompletionRate = stats.weeklyCompletionRate,
                        monthlyCompletionRate = stats.monthlyCompletionRate,
                        isLoading = false
                    )

                } catch (e: Exception) {

                    _uiState.value = StatisticsUiState(
                        isLoading = false,
                        error = e.message ?: "Không thể tải thống kê"
                    )
                }
            }
        }
    }
}
class StatisticsViewModelFactory(
    private val repository: HabitRepository
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}