package com.habitflow.app.core.domain.calculator

import com.habitflow.app.HabitStats
import com.habitflow.app.OccurrenceEntity
import com.habitflow.app.OccurrenceStatus

object StatisticsAggregator {

    fun calculate(
        items: List<OccurrenceEntity>,
        todayEpochDay: Long
    ): HabitStats {

        val ordered = items.sortedBy { it.scheduledEpochDay }

        // Tổng hợp trạng thái
        val completed = ordered.count {
            it.status == OccurrenceStatus.COMPLETED
        }

        val missed = ordered.count {
            it.status == OccurrenceStatus.MISSED
        }

        val skipped = ordered.count {
            it.status == OccurrenceStatus.SKIPPED
        }

        val frozen = ordered.count {
            it.status == OccurrenceStatus.FROZEN
        }

        // Tính longest streak
        var running = 0
        var longest = 0

        ordered
            .filter { it.status != OccurrenceStatus.PENDING }
            .forEach { occurrence ->

                when (occurrence.status) {

                    OccurrenceStatus.COMPLETED -> {
                        running++
                        longest = maxOf(longest, running)
                    }

                    OccurrenceStatus.MISSED -> {
                        running = 0
                    }

                    OccurrenceStatus.SKIPPED,
                    OccurrenceStatus.FROZEN,
                    OccurrenceStatus.PENDING -> {
                        // Không thay đổi streak
                    }
                }
            }

        // Tính current streak
        var current = 0

        for (item in ordered.asReversed()) {

            when (item.status) {

                OccurrenceStatus.COMPLETED -> {
                    current++
                }

                OccurrenceStatus.SKIPPED,
                OccurrenceStatus.FROZEN -> {
                    // Không phá streak
                }

                OccurrenceStatus.MISSED -> {
                    break
                }

                OccurrenceStatus.PENDING -> {
                    // Bỏ qua
                }
            }
        }

        // Completion rate tổng
        val totalConsidered = completed + missed

        val completionRate =
            if (totalConsidered == 0) {
                0.0
            } else {
                completed * 100.0 / totalConsidered
            }

        // 7 ngày gần nhất
        val last7Days = ordered.filter {
            it.scheduledEpochDay > todayEpochDay - 7
        }

        // 30 ngày gần nhất
        val last30Days = ordered.filter {
            it.scheduledEpochDay > todayEpochDay - 30
        }

        fun calculateRate(
            occurrences: List<OccurrenceEntity>
        ): Double {

            val completedCount = occurrences.count {
                it.status == OccurrenceStatus.COMPLETED
            }

            val missedCount = occurrences.count {
                it.status == OccurrenceStatus.MISSED
            }

            val considered = completedCount + missedCount

            return if (considered == 0) {
                0.0
            } else {
                completedCount * 100.0 / considered
            }
        }

        return HabitStats(
            completed = completed,
            missed = missed,
            skipped = skipped,
            frozen = frozen,
            currentStreak = current,
            longestStreak = longest,
            completionRate = completionRate,
            weeklyCompletionRate = calculateRate(last7Days),
            monthlyCompletionRate = calculateRate(last30Days)
        )
    }
}