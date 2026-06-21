package com.example.roadguideapp.goldhunt.panorama.journal.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalEntry
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalProgress
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalStatus
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class PanoramaHuntJournalFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatDiscoveredProgress(progress: PanoramaHuntJournalProgress): String =
        context.getString(
            R.string.panorama_hunt_journal_discovered_progress,
            progress.discoveredCount,
            progress.trackableCount,
        )

    fun formatCompletedProgress(progress: PanoramaHuntJournalProgress): String =
        context.getString(
            R.string.panorama_hunt_journal_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatMissingProgress(progress: PanoramaHuntJournalProgress): String =
        context.getString(
            R.string.panorama_hunt_journal_missing_progress,
            progress.missingCount,
            progress.trackableCount,
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun formatXp(value: Long): String = ExplorerProfileFormatters.formatXp(value)

    fun toEntryUiState(entry: PanoramaHuntJournalEntry): PanoramaHuntJournalEntryUiState =
        PanoramaHuntJournalEntryUiState(
            catalogKey = entry.huntType.id,
            displayName = entry.displayName,
            statusLabel = formatStatus(entry.status),
            firstDiscoveryLabel = formatDate(entry.firstDiscoveredAtMs, entry.isMissing),
            completionDateLabel = formatDate(entry.completedAtMs, !entry.isCompleted),
            rewardsLabel = formatCredits(entry.totalRewardsEarned),
            xpLabel = formatXp(entry.totalXpEarned),
            discoveredCountLabel = formatCount(entry.discoveredCount, entry.isMissing),
            completedCountLabel = formatCount(entry.completedCount, !entry.isCompleted),
            achievementLabel = entry.achievementKey?.let { formatHookLabel(it, entry.achievementUnlocked) },
            storyFragmentLabel = entry.storyFragmentKey?.let { formatHookLabel(it, entry.storyFragmentUnlocked) },
            legendaryRelicLabel = entry.legendaryRelicKey?.let { formatHookLabel(it, entry.legendaryRelicUnlocked) },
            isMissing = entry.isMissing,
            isDiscovered = entry.isDiscovered,
            isCompleted = entry.isCompleted,
        )

    private fun formatStatus(status: PanoramaHuntJournalStatus): String = when (status) {
        PanoramaHuntJournalStatus.MISSING ->
            context.getString(R.string.panorama_hunt_journal_status_missing)
        PanoramaHuntJournalStatus.DISCOVERED ->
            context.getString(R.string.panorama_hunt_journal_status_discovered)
        PanoramaHuntJournalStatus.COMPLETED ->
            context.getString(R.string.panorama_hunt_journal_status_completed)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.panorama_hunt_journal_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatCount(count: Int, unavailable: Boolean): String {
        if (unavailable) return context.getString(R.string.panorama_hunt_journal_not_recorded)
        return count.toString()
    }

    private fun formatHookLabel(key: String, unlocked: Boolean): String =
        if (unlocked) {
            context.getString(R.string.panorama_hunt_journal_hook_unlocked, key)
        } else {
            context.getString(R.string.panorama_hunt_journal_hook_locked, key)
        }
}
