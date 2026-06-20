package com.example.roadguideapp.goldhunt.achievements.collectionbook

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalStatus
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier

internal data class AchievementCollectionBookEntry(
    val achievementId: String,
    val displayName: String,
    val category: AchievementCategory,
    val status: AchievementJournalStatus,
    val rarityTier: AchievementRarityTier,
    val minimumExplorerLevel: Int,
    val currentValue: Int,
    val targetValue: Int,
    val progressFraction: Float,
    val completionDateMs: Long?,
    val badgeKey: String?,
    val badgeUnlocked: Boolean,
    val titleKey: String?,
    val titleUnlocked: Boolean,
    val storyFragmentKey: String?,
    val storyFragmentEarned: Boolean,
    val legendaryRelicKey: String?,
    val legendaryRelicEarned: Boolean,
) {
    val isCompleted: Boolean get() = status == AchievementJournalStatus.COMPLETED
    val isRare: Boolean get() = rarityTier == AchievementRarityTier.RARE
    val isLegendary: Boolean get() = rarityTier == AchievementRarityTier.LEGENDARY
}
