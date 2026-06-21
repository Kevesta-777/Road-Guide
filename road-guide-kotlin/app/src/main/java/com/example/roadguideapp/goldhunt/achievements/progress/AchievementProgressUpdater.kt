package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.Achievement

internal object AchievementProgressUpdater {
    fun applyValue(
        achievement: Achievement,
        proposedValue: Int,
        timestampMs: Long,
    ): AchievementProgressResult {
        if (achievement.completed) {
            return AchievementProgressResult(
                achievement = achievement,
                isNewCompletion = false,
                progressChanged = false,
            )
        }
        val nextValue = maxOf(achievement.currentValue, proposedValue.coerceAtLeast(0))
        if (nextValue == achievement.currentValue) {
            return AchievementProgressResult(
                achievement = achievement,
                isNewCompletion = false,
                progressChanged = false,
            )
        }
        val completed = nextValue >= achievement.targetValue
        val isNewCompletion = completed
        val updated = achievement.withProgress(
            currentValue = if (completed) achievement.targetValue else nextValue,
            completed = completed,
            completionDate = if (completed) timestampMs else null,
        )
        return AchievementProgressResult(
            achievement = updated,
            isNewCompletion = isNewCompletion,
            progressChanged = true,
        )
    }

    fun applyIncrement(
        achievement: Achievement,
        increment: Int,
        timestampMs: Long,
    ): AchievementProgressResult {
        if (achievement.completed || increment <= 0) {
            return AchievementProgressResult(
                achievement = achievement,
                isNewCompletion = false,
                progressChanged = false,
            )
        }
        return applyValue(
            achievement = achievement,
            proposedValue = achievement.currentValue + increment,
            timestampMs = timestampMs,
        )
    }
}
