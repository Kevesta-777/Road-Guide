package com.example.roadguideapp.goldhunt.achievements.collectionbook

import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntry
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntry

internal data class AchievementCollectionBook(
    val achievementEntries: List<AchievementCollectionBookEntry>,
    val badgeEntries: List<AchievementBadgeEntry>,
    val titleEntries: List<AchievementTitleEntry>,
    val progress: AchievementCollectionBookProgress,
) {
    val completedAchievements: List<AchievementCollectionBookEntry> =
        achievementEntries.filter { it.isCompleted }
    val rareAchievements: List<AchievementCollectionBookEntry> =
        achievementEntries.filter { it.isRare && it.isCompleted }
    val legendaryAchievements: List<AchievementCollectionBookEntry> =
        achievementEntries.filter { it.isLegendary && it.isCompleted }
    val unlockedBadges: List<AchievementBadgeEntry> = badgeEntries.filter { it.isUnlocked }
    val unlockedTitles: List<AchievementTitleEntry> = titleEntries.filter { it.isUnlocked }
}
