package com.example.roadguideapp.goldhunt.clusters.encyclopedia.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaEntry
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaProgress
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaStatus
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class ClusterEncyclopediaFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatDiscoveredProgress(progress: ClusterEncyclopediaProgress): String =
        context.getString(
            R.string.cluster_encyclopedia_discovered_progress,
            progress.discoveredCount,
            progress.trackableCount,
        )

    fun formatCompletedProgress(progress: ClusterEncyclopediaProgress): String =
        context.getString(
            R.string.cluster_encyclopedia_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun toEntryUiState(entry: ClusterEncyclopediaEntry): ClusterEncyclopediaEntryUiState =
        ClusterEncyclopediaEntryUiState(
            catalogKey = entry.clusterType.id,
            displayName = entry.displayName,
            statusLabel = formatStatus(entry.status),
            firstDiscoveryLabel = formatDate(entry.firstDiscoveredAtMs, entry.isMissing),
            completionDateLabel = formatDate(entry.completedAtMs, !entry.isCompleted),
            rewardsLabel = formatCredits(entry.totalRewardsEarned),
            discoveredCountLabel = formatCount(entry.discoveredCount, entry.isMissing),
            completedCountLabel = formatCount(entry.completedCount, !entry.isCompleted),
            isMissing = entry.isMissing,
            isDiscovered = entry.isDiscovered,
            isCompleted = entry.isCompleted,
        )

    private fun formatStatus(status: ClusterEncyclopediaStatus): String = when (status) {
        ClusterEncyclopediaStatus.MISSING ->
            context.getString(R.string.cluster_encyclopedia_status_missing)
        ClusterEncyclopediaStatus.DISCOVERED ->
            context.getString(R.string.cluster_encyclopedia_status_discovered)
        ClusterEncyclopediaStatus.COMPLETED ->
            context.getString(R.string.cluster_encyclopedia_status_completed)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.cluster_encyclopedia_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatCount(count: Int, unavailable: Boolean): String {
        if (unavailable) return context.getString(R.string.cluster_encyclopedia_not_recorded)
        return count.toString()
    }
}
