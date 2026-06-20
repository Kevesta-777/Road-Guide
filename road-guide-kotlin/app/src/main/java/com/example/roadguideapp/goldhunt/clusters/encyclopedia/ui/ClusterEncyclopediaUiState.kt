package com.example.roadguideapp.goldhunt.clusters.encyclopedia.ui

import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopedia
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaEntry
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaProgress

internal data class ClusterEncyclopediaEntryUiState(
    val catalogKey: String,
    val displayName: String,
    val statusLabel: String,
    val firstDiscoveryLabel: String,
    val completionDateLabel: String,
    val rewardsLabel: String,
    val discoveredCountLabel: String,
    val completedCountLabel: String,
    val isMissing: Boolean,
    val isDiscovered: Boolean,
    val isCompleted: Boolean,
)

internal data class ClusterEncyclopediaUiState(
    val discoveredProgressLabel: String,
    val completedProgressLabel: String,
    val discoveredProgressFraction: Float,
    val completionProgressFraction: Float,
    val totalRewardsLabel: String,
    val entries: List<ClusterEncyclopediaEntryUiState>,
) {
    companion object {
        val Empty = ClusterEncyclopediaUiState(
            discoveredProgressLabel = "",
            completedProgressLabel = "",
            discoveredProgressFraction = 0f,
            completionProgressFraction = 0f,
            totalRewardsLabel = "",
            entries = emptyList(),
        )

        fun from(encyclopedia: ClusterEncyclopedia, formatters: ClusterEncyclopediaFormatters): ClusterEncyclopediaUiState =
            from(encyclopedia.entries, encyclopedia.progress, formatters)

        fun from(
            entries: List<ClusterEncyclopediaEntry>,
            progress: ClusterEncyclopediaProgress,
            formatters: ClusterEncyclopediaFormatters,
        ): ClusterEncyclopediaUiState = ClusterEncyclopediaUiState(
            discoveredProgressLabel = formatters.formatDiscoveredProgress(progress),
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            discoveredProgressFraction = progress.discoveredFraction,
            completionProgressFraction = progress.completionFraction,
            totalRewardsLabel = formatters.formatCredits(progress.totalRewardsEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
        )
    }
}
