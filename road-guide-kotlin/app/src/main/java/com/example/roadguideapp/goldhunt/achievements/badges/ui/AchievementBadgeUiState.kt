package com.example.roadguideapp.goldhunt.achievements.badges.ui

import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeCollection
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntry

internal data class AchievementBadgeEntryUiState(
    val badgeKey: String,
    val title: String,
    val iconLabel: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val isUnlocked: Boolean,
    val isLocked: Boolean,
)

internal data class AchievementBadgeUiState(
    val unlockedProgressLabel: String,
    val lockedProgressLabel: String,
    val completionPercentageLabel: String,
    val completionProgressFraction: Float,
    val unlockedEntries: List<AchievementBadgeEntryUiState>,
    val lockedEntries: List<AchievementBadgeEntryUiState>,
) {
    companion object {
        val Empty = AchievementBadgeUiState(
            unlockedProgressLabel = "",
            lockedProgressLabel = "",
            completionPercentageLabel = "",
            completionProgressFraction = 0f,
            unlockedEntries = emptyList(),
            lockedEntries = emptyList(),
        )

        fun from(collection: AchievementBadgeCollection, formatters: AchievementBadgeFormatters): AchievementBadgeUiState {
            val uiEntries = collection.entries.map { formatters.toEntryUiState(it) }
            return AchievementBadgeUiState(
                unlockedProgressLabel = formatters.formatUnlockedProgress(collection.progress),
                lockedProgressLabel = formatters.formatLockedProgress(collection.progress),
                completionPercentageLabel = formatters.formatCompletionPercentage(collection.progress),
                completionProgressFraction = collection.progress.completionFraction,
                unlockedEntries = uiEntries.filter { it.isUnlocked },
                lockedEntries = uiEntries.filter { it.isLocked },
            )
        }
    }
}
