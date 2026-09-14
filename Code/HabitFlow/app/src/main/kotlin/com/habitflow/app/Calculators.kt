package com.habitflow.app

import java.time.LocalDate

object HabitStatisticsCalculator {
    fun calculate(occurrences: List<OccurrenceEntity>, habits: List<HabitEntity>, todayEpochDay: Long): HabitStats {
        if (habits.isEmpty()) return HabitStats()

        // Group occurrences by epoch day for rapid lookup
        val occurrencesByDay = occurrences.groupBy { it.scheduledEpochDay }

        // Determine when tracking started based on earliest habit creation
        val minHabitCreatedAt = habits.minOf { it.createdAt }
        val startEpochDay = minHabitCreatedAt / 86400000L

        var completed = 0
        var missed = 0
        var skipped = 0
        var frozen = 0

        var runningStreak = 0
        var longestStreak = 0

        // For weekly and monthly rates
        var weeklyCompleted = 0
        var weeklyMissed = 0
        var monthlyCompleted = 0
        var monthlyMissed = 0

        // Evaluate day by day from the very beginning up to today
        for (epochDay in startEpochDay..todayEpochDay) {
            val date = LocalDate.ofEpochDay(epochDay)
            val dayOfWeek = date.dayOfWeek.value

            // Habits scheduled for this day of the week and already created at this point
            val dayHabits = habits.filter { habit ->
                val isScheduled = habit.scheduledDays.isEmpty() || habit.scheduledDays.split(",").contains(dayOfWeek.toString())
                val isCreated = (habit.createdAt / 86400000L) <= epochDay
                isScheduled && isCreated
            }

            if (dayHabits.isEmpty()) {
                // No habits scheduled for this day; check if we should break streak if it's a past day? 
                // Usually, if no habit is scheduled, the streak is maintained/untouched.
                continue
            }

            val dayOccurrences = occurrencesByDay[epochDay] ?: emptyList()
            val occurrencesMap = dayOccurrences.associateBy { it.habitId }

            var dayHasCompleted = false
            var dayHasMissed = false
            var dayHasFrozenOrSkipped = false

            var dayCompletedCount = 0
            var dayMissedCount = 0

            for (habit in dayHabits) {
                val occurrence = occurrencesMap[habit.id]
                if (occurrence != null) {
                    when (occurrence.status) {
                        OccurrenceStatus.COMPLETED -> {
                            completed++
                            dayCompletedCount++
                            dayHasCompleted = true
                            if (epochDay > todayEpochDay - 7) weeklyCompleted++
                            if (epochDay > todayEpochDay - 30) monthlyCompleted++
                        }
                        OccurrenceStatus.SKIPPED -> {
                            skipped++
                            dayHasFrozenOrSkipped = true
                        }
                        OccurrenceStatus.FROZEN -> {
                            frozen++
                            dayHasFrozenOrSkipped = true
                        }
                        OccurrenceStatus.MISSED -> {
                            missed++
                            dayMissedCount++
                            dayHasMissed = true
                            if (epochDay > todayEpochDay - 7) weeklyMissed++
                            if (epochDay > todayEpochDay - 30) monthlyMissed++
                        }
                        OccurrenceStatus.PENDING -> {
                            if (epochDay < todayEpochDay) {
                                missed++
                                dayMissedCount++
                                dayHasMissed = true
                                if (epochDay > todayEpochDay - 7) weeklyMissed++
                                if (epochDay > todayEpochDay - 30) monthlyMissed++
                            }
                        }
                    }
                } else {
                    // No record in DB
                    if (epochDay < todayEpochDay) {
                        // Past day with no completion or explicit skip/freeze means it was missed
                        missed++
                        dayMissedCount++
                        dayHasMissed = true
                        if (epochDay > todayEpochDay - 7) weeklyMissed++
                        if (epochDay > todayEpochDay - 30) monthlyMissed++
                    } else {
                        // Today with no record yet is pending, don't penalize completion rate or break streak yet
                    }
                }
            }

            // Streak tracking logic per day
            if (epochDay < todayEpochDay) {
                if (dayHasCompleted) {
                    runningStreak++
                    longestStreak = maxOf(longestStreak, runningStreak)
                } else if (dayHasFrozenOrSkipped) {
                    // Protected by card, streak remains unchanged
                } else if (dayHasMissed || dayMissedCount > 0 || dayCompletedCount == 0) {
                    // Completely missed day breaks the streak
                    runningStreak = 0
                }
            } else {
                // Today: if they have completed habits, it builds/extends the streak immediately in UI
                if (dayHasCompleted) {
                    runningStreak++
                    longestStreak = maxOf(longestStreak, runningStreak)
                }
            }
        }

        val totalConsidered = completed + missed
        val completionRate = if (totalConsidered == 0) 0.0 else completed * 100.0 / totalConsidered

        val totalWeekly = weeklyCompleted + weeklyMissed
        val weeklyCompletionRate = if (totalWeekly == 0) 0.0 else weeklyCompleted * 100.0 / totalWeekly

        val totalMonthly = monthlyCompleted + monthlyMissed
        val monthlyCompletionRate = if (totalMonthly == 0) 0.0 else monthlyCompleted * 100.0 / totalMonthly

        // Calculate current streak directly from the running streak up to today
        val currentStreak = runningStreak

        return HabitStats(
            completed = completed,
            missed = missed,
            skipped = skipped,
            frozen = frozen,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = completionRate,
            weeklyCompletionRate = weeklyCompletionRate,
            monthlyCompletionRate = monthlyCompletionRate
        )
    }
}
