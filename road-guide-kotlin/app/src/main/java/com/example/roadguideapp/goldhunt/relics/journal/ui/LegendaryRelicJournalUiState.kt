package com.example.roadguideapp.goldhunt.relics.journal.ui

import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournal
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalEntry
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalProgress

internal data class LegendaryRelicJournalStoryChapterUiState(
    val chapterNumber: Int,
    val title: String,
    val body: String,
    val statusLabel: String,
    val unlockDateLabel: String,
    val unlocked: Boolean,
)

internal data class LegendaryRelicJournalStoryLoreUiState(
    val title: String,
    val body: String,
    val triggerLabel: String,
    val statusLabel: String,
    val unlockDateLabel: String,
    val unlocked: Boolean,
)

internal data class LegendaryRelicJournalStoryCompletionUiState(
    val title: String,
    val body: String,
    val storyFragmentLabel: String,
    val statusLabel: String,
    val unlockDateLabel: String,
    val unlocked: Boolean,
)

internal data class LegendaryRelicJournalStoryUiState(
    val chapters: List<LegendaryRelicJournalStoryChapterUiState>,
    val loreEntries: List<LegendaryRelicJournalStoryLoreUiState>,
    val completionStory: LegendaryRelicJournalStoryCompletionUiState?,
    val progressLabel: String,
)

internal data class LegendaryRelicJournalPieceUiState(
    val pieceNumber: Int,
    val statusLabel: String,
    val discoveryDateLabel: String,
    val sourceLabel: String,
    val isMissing: Boolean,
    val isMasked: Boolean,
)

internal data class LegendaryRelicJournalEntryUiState(
    val relicId: String,
    val displayName: String,
    val description: String,
    val categoryLabel: String,
    val rarityLabel: String,
    val statusLabel: String,
    val progressLabel: String,
    val progressFraction: Float,
    val piecesFoundLabel: String,
    val piecesMissingLabel: String,
    val completionDateLabel: String,
    val minimumExplorerLevelLabel: String,
    val rewardsLabel: String,
    val xpLabel: String,
    val badgeLabel: String?,
    val titleLabel: String?,
    val powerLabel: String?,
    val storyFragmentLabel: String?,
    val storyPreview: LegendaryRelicJournalStoryUiState?,
    val chainUnlocksLabel: String?,
    val rewardsGrantedLabel: String,
    val pieces: List<LegendaryRelicJournalPieceUiState>,
    val isCompleted: Boolean,
    val isInProgress: Boolean,
    val isMissing: Boolean,
    val isLocked: Boolean,
    val isHiddenMasked: Boolean,
)

internal data class LegendaryRelicJournalUiState(
    val completedProgressLabel: String,
    val completionPercentageLabel: String,
    val piecesFoundProgressLabel: String,
    val piecesMissingProgressLabel: String,
    val hiddenDiscoveredProgressLabel: String,
    val completionProgressFraction: Float,
    val pieceProgressFraction: Float,
    val piecesMissingProgressFraction: Float,
    val hiddenDiscoveryProgressFraction: Float,
    val totalRewardsLabel: String,
    val totalXpLabel: String,
    val entries: List<LegendaryRelicJournalEntryUiState>,
) {
    companion object {
        val Empty = LegendaryRelicJournalUiState(
            completedProgressLabel = "",
            completionPercentageLabel = "",
            piecesFoundProgressLabel = "",
            piecesMissingProgressLabel = "",
            hiddenDiscoveredProgressLabel = "",
            completionProgressFraction = 0f,
            pieceProgressFraction = 0f,
            piecesMissingProgressFraction = 0f,
            hiddenDiscoveryProgressFraction = 0f,
            totalRewardsLabel = "",
            totalXpLabel = "",
            entries = emptyList(),
        )

        fun from(journal: LegendaryRelicJournal, formatters: LegendaryRelicJournalFormatters): LegendaryRelicJournalUiState =
            from(journal.entries, journal.progress, formatters)

        fun from(
            entries: List<LegendaryRelicJournalEntry>,
            progress: LegendaryRelicJournalProgress,
            formatters: LegendaryRelicJournalFormatters,
        ): LegendaryRelicJournalUiState = LegendaryRelicJournalUiState(
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            completionPercentageLabel = formatters.formatCompletionPercentage(progress),
            piecesFoundProgressLabel = formatters.formatPiecesFoundProgress(progress),
            piecesMissingProgressLabel = formatters.formatPiecesMissingProgress(progress),
            hiddenDiscoveredProgressLabel = formatters.formatHiddenDiscoveredProgress(progress),
            completionProgressFraction = progress.completionFraction,
            pieceProgressFraction = progress.pieceCompletionFraction,
            piecesMissingProgressFraction = progress.piecesMissingFraction,
            hiddenDiscoveryProgressFraction = progress.hiddenDiscoveryFraction,
            totalRewardsLabel = formatters.formatCredits(progress.totalCreditsEarned),
            totalXpLabel = formatters.formatXp(progress.totalXpEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
        )
    }
}
