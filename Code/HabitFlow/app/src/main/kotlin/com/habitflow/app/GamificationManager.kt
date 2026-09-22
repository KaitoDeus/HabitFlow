package com.habitflow.app

import java.time.LocalDate
import kotlin.math.sqrt

object GamificationManager {
    const val XP_PER_COMPLETION = 10L
    const val STREAK_FREEZE_AWARD_DAYS = 7L
    const val SKIP_AWARD_LEVELS = 3 // Award skip every 3 levels

    fun calculateLevel(xp: Long): Int {
        return (sqrt(xp.toDouble() / 100.0).toInt() + 1).coerceAtLeast(1)
    }

    fun getXpForNextLevel(currentLevel: Int): Long {
        return (currentLevel * currentLevel * 100).toLong()
    }

    fun getUpcomingRewards(currentLevel: Int): List<String> {
        return (currentLevel + 1..currentLevel + 3).map { lvl ->
            val reward = when {
                lvl % 5 == 0 -> "❄️ 1 Thẻ Đóng Băng + ⏭️ 1 Thẻ Bỏ Qua"
                lvl % 3 == 0 -> "⏭️ 1 Thẻ Bỏ Qua"
                else -> "💎 50 XP Bonus"
            }
            "Cấp $lvl: $reward"
        }
    }

    /**
     * Logic to process XP and Skills when a habit is completed.
     */
    suspend fun processCompletion(
        repository: HabitRepository,
        currentStreak: Int,
        todayEpochDay: Long
    ) {
        val stats = repository.getUserStats()
        
        // Award XP: Base 10 + Streak * 1.5
        val streakBonus = (currentStreak * 1.5).toLong()
        val totalXpReward = XP_PER_COMPLETION + streakBonus
        
        val newXp = stats.xp + totalXpReward
        val newLevel = calculateLevel(newXp)
        
        // Award Streak Freeze card every 7 days of streak (if not already awarded today)
        var newStreakFreezes = stats.streakFreezes
        var newSkips = stats.skipsAvailable
        
        if (currentStreak > 0 && currentStreak % STREAK_FREEZE_AWARD_DAYS.toInt() == 0 && stats.lastAwardedStreakFreezeEpochDay != todayEpochDay) {
            newStreakFreezes += 1
        }
        
        // Award Skip card on level up if level is multiple of 3
        if (newLevel > stats.level && newLevel % SKIP_AWARD_LEVELS == 0) {
            newSkips += 1
        }
        
        repository.updateUserStats(stats.copy(
            xp = newXp,
            level = newLevel,
            streakFreezes = newStreakFreezes,
            skipsAvailable = newSkips,
            lastAwardedStreakFreezeEpochDay = if (newStreakFreezes > stats.streakFreezes) todayEpochDay else stats.lastAwardedStreakFreezeEpochDay
        ))
    }

    suspend fun processReset(
        repository: HabitRepository,
        currentStreak: Int,
        todayEpochDay: Long
    ) {
        val stats = repository.getUserStats()
        
        // Calculate exact XP that was awarded: Base 10 + Streak * 1.5
        val streakBonus = (currentStreak * 1.5).toLong()
        val totalXpReward = XP_PER_COMPLETION + streakBonus
        
        val newXp = (stats.xp - totalXpReward).coerceAtLeast(0L)
        val newLevel = calculateLevel(newXp)
        
        repository.updateUserStats(stats.copy(
            xp = newXp,
            level = newLevel
        ))
    }
    
    suspend fun useStreakFreeze(repository: HabitRepository): Boolean {
        val stats = repository.getUserStats()
        if (stats.streakFreezes > 0) {
            repository.updateUserStats(stats.copy(streakFreezes = stats.streakFreezes - 1))
            return true
        }
        return false
    }

    suspend fun useSkipCard(repository: HabitRepository): Boolean {
        val stats = repository.getUserStats()
        if (stats.skipsAvailable > 0) {
            repository.updateUserStats(stats.copy(skipsAvailable = stats.skipsAvailable - 1))
            return true
        }
        return false
    }

    suspend fun skipLevel(repository: HabitRepository) {
        val stats = repository.getUserStats()
        val nextLevel = stats.level + 1
        val xpRequired = getXpForNextLevel(stats.level)
        repository.updateUserStats(stats.copy(level = nextLevel, xp = xpRequired))
    }
}
