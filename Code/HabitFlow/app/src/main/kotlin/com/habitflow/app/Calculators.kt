package com.habitflow.app

object HabitStatisticsCalculator {
    fun calculate(items: List<OccurrenceEntity>): HabitStats {
        val ordered = items.sortedBy { it.scheduledEpochDay }
        val completed = ordered.count { it.status == OccurrenceStatus.COMPLETED }
        val missed = ordered.count { it.status == OccurrenceStatus.MISSED }
        val skipped = ordered.count { it.status == OccurrenceStatus.SKIPPED }
        var running = 0
        var longest = 0
        ordered.filter { it.status != OccurrenceStatus.PENDING }.forEach {
            when (it.status) {
                OccurrenceStatus.COMPLETED -> { running++; longest = maxOf(longest, running) }
                OccurrenceStatus.MISSED -> running = 0
                OccurrenceStatus.SKIPPED, OccurrenceStatus.PENDING -> Unit
            }
        }
        var current = 0
        for (item in ordered.asReversed()) {
            when (item.status) {
                OccurrenceStatus.COMPLETED -> current++
                OccurrenceStatus.SKIPPED -> Unit
                OccurrenceStatus.MISSED -> break
                OccurrenceStatus.PENDING -> Unit
            }
        }
        val total = completed + missed
        return HabitStats(completed, missed, skipped, current, longest,
            if (total == 0) 0.0 else completed * 100.0 / total)
    }
}
