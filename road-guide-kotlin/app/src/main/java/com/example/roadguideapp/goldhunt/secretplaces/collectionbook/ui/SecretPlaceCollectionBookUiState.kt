package com.example.roadguideapp.goldhunt.secretplaces.collectionbook.ui

import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBook
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookEntry
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookProgress

internal data class SecretPlaceCollectionBookEntryUiState(
    val catalogKey: String,
    val displayName: String,
    val statusLabel: String,
    val firstDiscoveryLabel: String,
    val completionDateLabel: String,
    val creditsLabel: String,
    val xpLabel: String,
    val discoveredCountLabel: String,
    val completedCountLabel: String,
    val explorerLevelLabel: String,
    val achievementLabel: String?,
    val storyFragmentLabel: String?,
    val isMissing: Boolean,
    val isDiscovered: Boolean,
    val isCompleted: Boolean,
)

internal data class SecretPlaceCollectionBookUiState(
    val discoveredProgressLabel: String,
    val completedProgressLabel: String,
    val missingProgressLabel: String,
    val discoveredProgressFraction: Float,
    val completionProgressFraction: Float,
    val totalCreditsLabel: String,
    val totalXpLabel: String,
    val entries: List<SecretPlaceCollectionBookEntryUiState>,
) {
    companion object {
        val Empty = SecretPlaceCollectionBookUiState(
            discoveredProgressLabel = "",
            completedProgressLabel = "",
            missingProgressLabel = "",
            discoveredProgressFraction = 0f,
            completionProgressFraction = 0f,
            totalCreditsLabel = "",
            totalXpLabel = "",
            entries = emptyList(),
        )

        fun from(
            book: SecretPlaceCollectionBook,
            formatters: SecretPlaceCollectionBookFormatters,
        ): SecretPlaceCollectionBookUiState = from(book.entries, book.progress, formatters)

        fun from(
            entries: List<SecretPlaceCollectionBookEntry>,
            progress: SecretPlaceCollectionBookProgress,
            formatters: SecretPlaceCollectionBookFormatters,
        ): SecretPlaceCollectionBookUiState = SecretPlaceCollectionBookUiState(
            discoveredProgressLabel = formatters.formatDiscoveredProgress(progress),
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            missingProgressLabel = formatters.formatMissingProgress(progress),
            discoveredProgressFraction = progress.discoveredFraction,
            completionProgressFraction = progress.completionFraction,
            totalCreditsLabel = formatters.formatCredits(progress.totalCreditsEarned),
            totalXpLabel = formatters.formatXp(progress.totalXpEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
        )
    }
}
