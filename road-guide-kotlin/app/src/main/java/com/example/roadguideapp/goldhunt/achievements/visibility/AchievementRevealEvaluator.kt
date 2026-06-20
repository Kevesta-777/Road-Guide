package com.example.roadguideapp.goldhunt.achievements.visibility

import com.example.roadguideapp.goldhunt.achievements.Achievement

/**
 * Determines when achievement details may be shown in player-facing surfaces.
 *
 * Hidden and secret achievements reveal only after completion. The progress engine
 * continues tracking all tiers regardless of visibility.
 */
internal object AchievementRevealEvaluator {
    fun isRevealed(visibility: AchievementVisibility, achievement: Achievement): Boolean =
        visibility == AchievementVisibility.VISIBLE || achievement.completed

    fun shouldIncludeInJournal(visibility: AchievementVisibility, achievement: Achievement): Boolean =
        visibility != AchievementVisibility.SECRET || achievement.completed
}
