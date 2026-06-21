package com.example.roadguideapp.goldhunt.events.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookEntry
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookFamilyEntry
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookProgress
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookStatus
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class SeasonalEventCollectionBookFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatParticipatedProgress(progress: SeasonalEventCollectionBookProgress): String =
        context.getString(
            R.string.seasonal_event_collection_book_participated_progress,
            progress.familiesParticipated,
            progress.trackableFamilies,
        )

    fun formatCompletedProgress(progress: SeasonalEventCollectionBookProgress): String =
        context.getString(
            R.string.seasonal_event_collection_book_completed_progress,
            progress.familiesCompleted,
            progress.trackableFamilies,
        )

    fun formatMissingProgress(progress: SeasonalEventCollectionBookProgress): String =
        context.getString(
            R.string.seasonal_event_collection_book_missing_progress,
            progress.familiesMissing,
            progress.trackableFamilies,
        )

    fun formatCompletedCyclesProgress(progress: SeasonalEventCollectionBookProgress): String =
        context.getString(
            R.string.seasonal_event_collection_book_completed_cycles_progress,
            progress.completedCycles,
            progress.participatedCycles.coerceAtLeast(progress.completedCycles),
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun formatXp(value: Long): String = ExplorerProfileFormatters.formatXp(value)

    fun formatCount(value: Int): String = value.toString()

    fun toCycleUiState(entry: SeasonalEventCollectionBookEntry): SeasonalEventCollectionBookCycleUiState =
        SeasonalEventCollectionBookCycleUiState(
            eventId = entry.eventId,
            displayName = entry.displayName,
            cycleYearLabel = entry.cycleYear.toString(),
            statusLabel = formatStatus(entry.status),
            participatedDateLabel = formatDate(entry.firstParticipatedAtMs, entry.isMissing),
            completionDateLabel = formatDate(entry.completedAtMs, !entry.isCompleted),
            rewardsLabel = formatCredits(entry.totalCreditsEarned),
            xpLabel = formatXp(entry.totalXpEarned),
            fragmentsLabel = formatCount(entry.fragmentsEarned),
            achievementsEarnedLabel = formatCount(entry.achievementsEarned),
            participationAchievementLabel = entry.participationAchievementKey?.let {
                formatHookLabel(it, entry.participationAchievementEarned)
            },
            completionAchievementLabel = entry.completionAchievementKey?.let {
                formatHookLabel(it, entry.completionAchievementEarned)
            },
            masteryAchievementLabel = entry.masteryAchievementKey?.let {
                formatHookLabel(it, entry.masteryAchievementEarned)
            },
            storyFragmentLabel = entry.storyFragmentKey?.let {
                formatHookLabel(it, entry.storyFragmentEarned)
            },
            legendaryRelicLabel = entry.legendaryRelicKey?.let {
                formatHookLabel(it, entry.legendaryRelicEarned)
            },
            isParticipated = entry.isParticipated,
            isCompleted = entry.isCompleted,
        )

    fun toFamilyUiState(family: SeasonalEventCollectionBookFamilyEntry): SeasonalEventCollectionBookFamilyUiState =
        SeasonalEventCollectionBookFamilyUiState(
            displayName = family.displayName,
            statusLabel = context.getString(R.string.seasonal_event_collection_book_status_missing),
            minExplorerLevelLabel = context.getString(
                R.string.seasonal_event_collection_book_min_level,
                family.minExplorerLevel,
            ),
        )

    private fun formatStatus(status: SeasonalEventCollectionBookStatus): String = when (status) {
        SeasonalEventCollectionBookStatus.MISSING ->
            context.getString(R.string.seasonal_event_collection_book_status_missing)
        SeasonalEventCollectionBookStatus.PARTICIPATED ->
            context.getString(R.string.seasonal_event_collection_book_status_participated)
        SeasonalEventCollectionBookStatus.COMPLETED ->
            context.getString(R.string.seasonal_event_collection_book_status_completed)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.seasonal_event_collection_book_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatHookLabel(key: String, unlocked: Boolean): String =
        if (unlocked) {
            context.getString(R.string.seasonal_event_collection_book_hook_unlocked, key)
        } else {
            context.getString(R.string.seasonal_event_collection_book_hook_locked, key)
        }
}
