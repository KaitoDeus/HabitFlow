package com.habitflow.app.feature.statistics

data class StatisticsUiState(
    val completed: Int = 0,
    val missed: Int = 0,
    val skipped: Int = 0,
    val frozen: Int = 0,

    val currentStreak: Int = 0,
    val longestStreak: Int = 0,

    val completionRate: Double = 0.0,
    val weeklyCompletionRate: Double = 0.0,
    val monthlyCompletionRate: Double = 0.0,

    val isLoading: Boolean = false,
    val error: String? = null,
)