package com.example.roadguideapp.goldhunt.secretplaces.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookEntry
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookProgress
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookStatus
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class SecretPlaceCollectionBookFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatDiscoveredProgress(progress: SecretPlaceCollectionBookProgress): String =
        context.getString(
            R.string.secret_place_collection_book_discovered_progress,
            progress.discoveredCount,
            progress.trackableCount,
        )

    fun formatCompletedProgress(progress: SecretPlaceCollectionBookProgress): String =
        context.getString(
            R.string.secret_place_collection_book_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatMissingProgress(progress: SecretPlaceCollectionBookProgress): String =
        context.getString(
            R.string.secret_place_collection_book_missing_progress,
            progress.missingCount,
            progress.trackableCount,
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun formatXp(value: Long): String = ExplorerProfileFormatters.formatXp(value)

    fun toEntryUiState(entry: SecretPlaceCollectionBookEntry): SecretPlaceCollectionBookEntryUiState =
        SecretPlaceCollectionBookEntryUiState(
            catalogKey = entry.category.id,
            displayName = entry.displayName,
            statusLabel = formatStatus(entry.status),
            firstDiscoveryLabel = formatDate(entry.firstDiscoveredAtMs, entry.isMissing),
            completionDateLabel = formatDate(entry.completedAtMs, !entry.isCompleted),
            creditsLabel = formatCredits(entry.totalCreditsEarned),
            xpLabel = formatXp(entry.totalXpEarned),
            discoveredCountLabel = formatCount(entry.discoveredCount, entry.isMissing),
            completedCountLabel = formatCount(entry.completedCount, !entry.isCompleted),
            explorerLevelLabel = context.getString(
                R.string.secret_place_collection_book_explorer_level_value,
                entry.minimumExplorerLevel,
            ),
            achievementLabel = entry.achievementKey?.let { formatHookLabel(it) },
            storyFragmentLabel = entry.storyFragmentKey?.let { formatHookLabel(it) },
            isMissing = entry.isMissing,
            isDiscovered = entry.isDiscovered,
            isCompleted = entry.isCompleted,
        )

    private fun formatStatus(status: SecretPlaceCollectionBookStatus): String = when (status) {
        SecretPlaceCollectionBookStatus.MISSING ->
            context.getString(R.string.secret_place_collection_book_status_missing)
        SecretPlaceCollectionBookStatus.DISCOVERED ->
            context.getString(R.string.secret_place_collection_book_status_discovered)
        SecretPlaceCollectionBookStatus.COMPLETED ->
            context.getString(R.string.secret_place_collection_book_status_completed)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.secret_place_collection_book_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatCount(count: Int, unavailable: Boolean): String {
        if (unavailable) return context.getString(R.string.secret_place_collection_book_not_recorded)
        return count.toString()
    }

    private fun formatHookLabel(key: String): String = key
}
