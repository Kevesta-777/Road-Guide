package com.example.roadguideapp.goldhunt.events.collectionbook.ui

import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBook
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookEntry
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookFamilyEntry
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookProgress

internal data class SeasonalEventCollectionBookCycleUiState(
    val eventId: String,
    val displayName: String,
    val cycleYearLabel: String,
    val statusLabel: String,
    val participatedDateLabel: String,
    val completionDateLabel: String,
    val rewardsLabel: String,
    val xpLabel: String,
    val fragmentsLabel: String,
    val achievementsEarnedLabel: String,
    val participationAchievementLabel: String?,
    val completionAchievementLabel: String?,
    val masteryAchievementLabel: String?,
    val storyFragmentLabel: String?,
    val legendaryRelicLabel: String?,
    val isParticipated: Boolean,
    val isCompleted: Boolean,
)

internal data class SeasonalEventCollectionBookFamilyUiState(
    val displayName: String,
    val statusLabel: String,
    val minExplorerLevelLabel: String,
)

internal data class SeasonalEventCollectionBookUiState(
    val participatedProgressLabel: String,
    val completedProgressLabel: String,
    val missingProgressLabel: String,
    val participatedProgressFraction: Float,
    val completedProgressFraction: Float,
    val missingProgressFraction: Float,
    val totalRewardsLabel: String,
    val totalXpLabel: String,
    val totalAchievementsEarned: Int,
    val cycleEntries: List<SeasonalEventCollectionBookCycleUiState>,
    val missingFamilies: List<SeasonalEventCollectionBookFamilyUiState>,
) {
    companion object {
        val Empty = SeasonalEventCollectionBookUiState(
            participatedProgressLabel = "",
            completedProgressLabel = "",
            missingProgressLabel = "",
            participatedProgressFraction = 0f,
            completedProgressFraction = 0f,
            missingProgressFraction = 0f,
            totalRewardsLabel = "",
            totalXpLabel = "",
            totalAchievementsEarned = 0,
            cycleEntries = emptyList(),
            missingFamilies = emptyList(),
        )

        fun from(
            book: SeasonalEventCollectionBook,
            formatters: SeasonalEventCollectionBookFormatters,
        ): SeasonalEventCollectionBookUiState = from(
            cycleEntries = book.cycleEntries,
            missingFamilies = book.missingFamilies,
            progress = book.progress,
            formatters = formatters,
        )

        fun from(
            cycleEntries: List<SeasonalEventCollectionBookEntry>,
            missingFamilies: List<SeasonalEventCollectionBookFamilyEntry>,
            progress: SeasonalEventCollectionBookProgress,
            formatters: SeasonalEventCollectionBookFormatters,
        ): SeasonalEventCollectionBookUiState = SeasonalEventCollectionBookUiState(
            participatedProgressLabel = formatters.formatParticipatedProgress(progress),
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            missingProgressLabel = formatters.formatMissingProgress(progress),
            participatedProgressFraction = progress.participatedFraction,
            completedProgressFraction = progress.completedFraction,
            missingProgressFraction = progress.missingFraction,
            totalRewardsLabel = formatters.formatCredits(progress.totalCreditsEarned),
            totalXpLabel = formatters.formatXp(progress.totalXpEarned),
            totalAchievementsEarned = progress.totalAchievementsEarned,
            cycleEntries = cycleEntries.map { formatters.toCycleUiState(it) },
            missingFamilies = missingFamilies.map { formatters.toFamilyUiState(it) },
        )
    }
}
