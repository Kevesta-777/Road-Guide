package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.Achievement

internal data class AchievementProgressResult(
    val achievement: Achievement?,
    val isNewCompletion: Boolean,
    val progressChanged: Boolean,
) {
    companion object {
        val skipped = AchievementProgressResult(
            achievement = null,
            isNewCompletion = false,
            progressChanged = false,
        )
    }
}

internal data class AchievementProgressBatchResult(
    val results: List<AchievementProgressResult>,
) {
    val newCompletions: List<Achievement> =
        results.filter { it.isNewCompletion }.mapNotNull { it.achievement }

    val progressChangedCount: Int =
        results.count { it.progressChanged }
}
