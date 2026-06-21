package com.example.roadguideapp.goldhunt.panorama.journal.ui

import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournal
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalEntry
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalProgress

internal data class PanoramaHuntJournalEntryUiState(
    val catalogKey: String,
    val displayName: String,
    val statusLabel: String,
    val firstDiscoveryLabel: String,
    val completionDateLabel: String,
    val rewardsLabel: String,
    val xpLabel: String,
    val discoveredCountLabel: String,
    val completedCountLabel: String,
    val achievementLabel: String?,
    val storyFragmentLabel: String?,
    val legendaryRelicLabel: String?,
    val isMissing: Boolean,
    val isDiscovered: Boolean,
    val isCompleted: Boolean,
)

internal data class PanoramaHuntJournalUiState(
    val discoveredProgressLabel: String,
    val completedProgressLabel: String,
    val missingProgressLabel: String,
    val discoveredProgressFraction: Float,
    val completionProgressFraction: Float,
    val missingProgressFraction: Float,
    val totalRewardsLabel: String,
    val totalXpLabel: String,
    val entries: List<PanoramaHuntJournalEntryUiState>,
) {
    companion object {
        val Empty = PanoramaHuntJournalUiState(
            discoveredProgressLabel = "",
            completedProgressLabel = "",
            missingProgressLabel = "",
            discoveredProgressFraction = 0f,
            completionProgressFraction = 0f,
            missingProgressFraction = 0f,
            totalRewardsLabel = "",
            totalXpLabel = "",
            entries = emptyList(),
        )

        fun from(journal: PanoramaHuntJournal, formatters: PanoramaHuntJournalFormatters): PanoramaHuntJournalUiState =
            from(journal.entries, journal.progress, formatters)

        fun from(
            entries: List<PanoramaHuntJournalEntry>,
            progress: PanoramaHuntJournalProgress,
            formatters: PanoramaHuntJournalFormatters,
        ): PanoramaHuntJournalUiState = PanoramaHuntJournalUiState(
            discoveredProgressLabel = formatters.formatDiscoveredProgress(progress),
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            missingProgressLabel = formatters.formatMissingProgress(progress),
            discoveredProgressFraction = progress.discoveredFraction,
            completionProgressFraction = progress.completionFraction,
            missingProgressFraction = progress.missingFraction,
            totalRewardsLabel = formatters.formatCredits(progress.totalRewardsEarned),
            totalXpLabel = formatters.formatXp(progress.totalXpEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
        )
    }
}
