package com.example.roadguideapp.goldhunt.achievements.journal.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalEntry
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalProgress
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRewardPreview
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalStatus
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class AchievementJournalFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatCompletedProgress(progress: AchievementJournalProgress): String =
        context.getString(
            R.string.achievement_journal_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatIncompleteProgress(progress: AchievementJournalProgress): String =
        context.getString(
            R.string.achievement_journal_incomplete_progress,
            progress.incompleteCount,
            progress.trackableCount,
        )

    fun formatLockedProgress(progress: AchievementJournalProgress): String =
        context.getString(
            R.string.achievement_journal_locked_progress,
            progress.lockedCount,
            progress.trackableCount,
        )

    fun formatHiddenProgress(progress: AchievementJournalProgress): String =
        context.getString(
            R.string.achievement_journal_hidden_progress,
            progress.hiddenCount,
            progress.trackableCount,
        )

    fun formatSecretProgress(progress: AchievementJournalProgress): String? {
        if (progress.undiscoveredSecretCount <= 0) return null
        return context.getString(
            R.string.achievement_journal_secret_progress,
            progress.undiscoveredSecretCount,
        )
    }

    fun formatCompletionPercentage(progress: AchievementJournalProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun toEntryUiState(entry: AchievementJournalEntry): AchievementJournalEntryUiState =
        AchievementJournalEntryUiState(
            achievementId = entry.achievementId,
            displayName = entry.title,
            categoryLabel = entry.category.displayName,
            statusLabel = formatStatus(entry.status),
            progressLabel = formatEntryProgress(entry),
            progressFraction = entry.progressFraction,
            completionDateLabel = formatDate(entry.completionDateMs, !entry.isCompleted),
            minimumLevelLabel = formatMinimumLevel(entry.minimumExplorerLevel, entry.isLocked),
            rewardPreviewLabel = formatRewardPreview(entry.rewardPreview, entry.isHidden),
            description = entry.description,
            isCompleted = entry.isCompleted,
            isIncomplete = entry.isIncomplete,
            isLocked = entry.isLocked,
            isHidden = entry.isHidden,
        )

    private fun formatStatus(status: AchievementJournalStatus): String = when (status) {
        AchievementJournalStatus.COMPLETED ->
            context.getString(R.string.achievement_journal_status_completed)
        AchievementJournalStatus.INCOMPLETE ->
            context.getString(R.string.achievement_journal_status_incomplete)
        AchievementJournalStatus.LOCKED ->
            context.getString(R.string.achievement_journal_status_locked)
        AchievementJournalStatus.HIDDEN ->
            context.getString(R.string.achievement_journal_status_hidden)
    }

    private fun formatEntryProgress(entry: AchievementJournalEntry): String {
        if (entry.isHidden) {
            return context.getString(R.string.achievement_journal_progress_hidden)
        }
        return context.getString(
            R.string.achievement_journal_entry_progress,
            entry.currentValue,
            entry.targetValue,
            ExplorerProfileFormatters.formatCompletionPercentage(entry.progressFraction),
        )
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.achievement_journal_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatMinimumLevel(level: Int, show: Boolean): String? {
        if (!show) return null
        return context.getString(R.string.achievement_journal_minimum_level, level)
    }

    private fun formatRewardPreview(
        preview: AchievementJournalRewardPreview,
        hidden: Boolean,
    ): String {
        if (hidden) {
            return context.getString(R.string.achievement_journal_reward_hidden)
        }
        val parts = buildList {
            if (preview.hasCredits) {
                add(context.getString(R.string.achievement_journal_reward_credits, preview.credits))
            }
            if (preview.hasXp) {
                add(context.getString(R.string.achievement_journal_reward_xp, preview.xp))
            }
            if (preview.hasTitle) {
                add(context.getString(R.string.achievement_journal_reward_title, preview.titleKey))
            }
            if (preview.hasRelic) {
                add(context.getString(R.string.achievement_journal_reward_relic, preview.legendaryRelicKey))
            }
            if (preview.hasStoryFragment) {
                add(context.getString(R.string.achievement_journal_reward_story, preview.storyFragmentKey))
            }
            if (preview.hasBadge) {
                add(context.getString(R.string.achievement_journal_reward_badge, preview.badgeKey))
            }
            preview.chainUnlockAchievementKeys.forEach { key ->
                add(context.getString(R.string.achievement_journal_reward_chain_achievement, key))
            }
            preview.chainBadgeKeys.forEach { key ->
                add(context.getString(R.string.achievement_journal_reward_chain_badge, key))
            }
            preview.chainTitleKeys.forEach { key ->
                add(context.getString(R.string.achievement_journal_reward_chain_title, key))
            }
            preview.chainLegendaryRelicKeys.forEach { key ->
                add(context.getString(R.string.achievement_journal_reward_chain_relic, key))
            }
            preview.chainStoryFragmentKeys.forEach { key ->
                add(context.getString(R.string.achievement_journal_reward_chain_story, key))
            }
        }
        return if (parts.isEmpty()) {
            context.getString(R.string.achievement_journal_reward_none)
        } else {
            parts.joinToString(" · ")
        }
    }
}
