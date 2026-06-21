package com.example.roadguideapp.goldhunt.achievements.collectionbook.ui

import com.example.roadguideapp.goldhunt.achievements.collectionbook.AchievementCollectionBook

internal data class AchievementCollectionBookAchievementUiState(
    val achievementId: String,
    val displayName: String,
    val categoryLabel: String,
    val statusLabel: String,
    val rarityLabel: String,
    val progressLabel: String,
    val rewardHooksLabel: String,
    val minimumLevelLabel: String?,
    val completionDateLabel: String,
    val isCompleted: Boolean,
    val isRare: Boolean,
    val isLegendary: Boolean,
)

internal data class AchievementCollectionBookBadgeUiState(
    val badgeKey: String,
    val displayName: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val isUnlocked: Boolean,
)

internal data class AchievementCollectionBookTitleUiState(
    val titleKey: String,
    val displayName: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val isUnlocked: Boolean,
    val isActive: Boolean,
)

internal data class AchievementCollectionBookUiState(
    val completedProgressLabel: String,
    val explorerLevelLabel: String,
    val completionPercentageLabel: String,
    val rareLegendaryLabel: String,
    val badgesProgressLabel: String,
    val titlesProgressLabel: String,
    val storyRelicProgressLabel: String,
    val rewardTotalsLabel: String,
    val completionProgressFraction: Float,
    val rareAchievements: List<AchievementCollectionBookAchievementUiState>,
    val legendaryAchievements: List<AchievementCollectionBookAchievementUiState>,
    val completedAchievements: List<AchievementCollectionBookAchievementUiState>,
    val unlockedBadges: List<AchievementCollectionBookBadgeUiState>,
    val lockedBadges: List<AchievementCollectionBookBadgeUiState>,
    val unlockedTitles: List<AchievementCollectionBookTitleUiState>,
    val lockedTitles: List<AchievementCollectionBookTitleUiState>,
) {
    companion object {
        val Empty = AchievementCollectionBookUiState(
            completedProgressLabel = "",
            explorerLevelLabel = "",
            completionPercentageLabel = "",
            rareLegendaryLabel = "",
            badgesProgressLabel = "",
            titlesProgressLabel = "",
            storyRelicProgressLabel = "",
            rewardTotalsLabel = "",
            completionProgressFraction = 0f,
            rareAchievements = emptyList(),
            legendaryAchievements = emptyList(),
            completedAchievements = emptyList(),
            unlockedBadges = emptyList(),
            lockedBadges = emptyList(),
            unlockedTitles = emptyList(),
            lockedTitles = emptyList(),
        )

        fun from(
            book: AchievementCollectionBook,
            formatters: AchievementCollectionBookFormatters,
        ): AchievementCollectionBookUiState {
            val badgeUi = book.badgeEntries.map { formatters.toBadgeUiState(it) }
            val titleUi = book.titleEntries.map { formatters.toTitleUiState(it) }
            return AchievementCollectionBookUiState(
                completedProgressLabel = formatters.formatCompletedProgress(book.progress),
                explorerLevelLabel = formatters.formatExplorerLevel(book.progress),
                completionPercentageLabel = formatters.formatCompletionPercentage(book.progress),
                rareLegendaryLabel = formatters.formatRareLegendary(book.progress),
                badgesProgressLabel = formatters.formatBadgesProgress(book.progress),
                titlesProgressLabel = formatters.formatTitlesProgress(book.progress),
                storyRelicProgressLabel = formatters.formatStoryRelicProgress(book.progress),
                rewardTotalsLabel = formatters.formatRewardTotals(book.progress),
                completionProgressFraction = book.progress.completionFraction,
                rareAchievements = book.rareAchievements.map { formatters.toAchievementUiState(it) },
                legendaryAchievements = book.legendaryAchievements.map { formatters.toAchievementUiState(it) },
                completedAchievements = book.completedAchievements.map { formatters.toAchievementUiState(it) },
                unlockedBadges = badgeUi.filter { it.isUnlocked },
                lockedBadges = badgeUi.filter { !it.isUnlocked },
                unlockedTitles = titleUi.filter { it.isUnlocked },
                lockedTitles = titleUi.filter { !it.isUnlocked },
            )
        }
    }
}
