package com.example.roadguideapp.goldhunt.achievements.titles.ui

import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleCollection

internal data class AchievementTitleEntryUiState(
    val titleKey: String,
    val displayTitle: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val actionLabel: String?,
    val isUnlocked: Boolean,
    val isLocked: Boolean,
    val isActive: Boolean,
    val canActivate: Boolean,
)

internal data class AchievementTitleUiState(
    val unlockedProgressLabel: String,
    val lockedProgressLabel: String,
    val completionPercentageLabel: String,
    val completionProgressFraction: Float,
    val activeTitleLabel: String?,
    val publicDisplayTitle: String,
    val canClearActiveTitle: Boolean,
    val unlockedEntries: List<AchievementTitleEntryUiState>,
    val lockedEntries: List<AchievementTitleEntryUiState>,
) {
    companion object {
        val Empty = AchievementTitleUiState(
            unlockedProgressLabel = "",
            lockedProgressLabel = "",
            completionPercentageLabel = "",
            completionProgressFraction = 0f,
            activeTitleLabel = null,
            publicDisplayTitle = "",
            canClearActiveTitle = false,
            unlockedEntries = emptyList(),
            lockedEntries = emptyList(),
        )

        fun from(
            collection: AchievementTitleCollection,
            publicDisplayTitle: String,
            formatters: AchievementTitleFormatters,
        ): AchievementTitleUiState {
            val uiEntries = collection.entries.map { formatters.toEntryUiState(it) }
            return AchievementTitleUiState(
                unlockedProgressLabel = formatters.formatUnlockedProgress(collection.progress),
                lockedProgressLabel = formatters.formatLockedProgress(collection.progress),
                completionPercentageLabel = formatters.formatCompletionPercentage(collection.progress),
                completionProgressFraction = collection.progress.completionFraction,
                activeTitleLabel = formatters.formatActiveTitleLabel(collection.activeEntry?.displayTitle),
                publicDisplayTitle = publicDisplayTitle,
                canClearActiveTitle = !collection.activeTitleKey.isNullOrBlank(),
                unlockedEntries = uiEntries.filter { it.isUnlocked },
                lockedEntries = uiEntries.filter { it.isLocked },
            )
        }
    }
}
