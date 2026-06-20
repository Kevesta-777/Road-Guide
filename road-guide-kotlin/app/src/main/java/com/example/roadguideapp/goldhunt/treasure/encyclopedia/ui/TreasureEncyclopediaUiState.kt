package com.example.roadguideapp.goldhunt.treasure.encyclopedia.ui

import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopedia
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaEntry
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaProgress

internal data class TreasureEncyclopediaEntryUiState(
    val catalogKey: String,
    val displayName: String,
    val statusLabel: String,
    val rarityLabel: String,
    val firstDiscoveryLabel: String,
    val creditsLabel: String,
    val collectionCountLabel: String,
    val isFound: Boolean,
    val isMissing: Boolean,
    val isLocked: Boolean,
)

internal data class TreasureEncyclopediaUiState(
    val progressLabel: String,
    val progressFraction: Float,
    val totalCreditsLabel: String,
    val entries: List<TreasureEncyclopediaEntryUiState>,
) {
    companion object {
        val Empty = TreasureEncyclopediaUiState(
            progressLabel = "",
            progressFraction = 0f,
            totalCreditsLabel = "",
            entries = emptyList(),
        )

        fun from(encyclopedia: TreasureEncyclopedia, formatters: TreasureEncyclopediaFormatters): TreasureEncyclopediaUiState =
            from(encyclopedia.entries, encyclopedia.progress, formatters)

        fun from(
            entries: List<TreasureEncyclopediaEntry>,
            progress: TreasureEncyclopediaProgress,
            formatters: TreasureEncyclopediaFormatters,
        ): TreasureEncyclopediaUiState = TreasureEncyclopediaUiState(
            progressLabel = formatters.formatProgress(progress),
            progressFraction = progress.completionFraction,
            totalCreditsLabel = formatters.formatCredits(progress.totalCreditsEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
        )
    }
}
