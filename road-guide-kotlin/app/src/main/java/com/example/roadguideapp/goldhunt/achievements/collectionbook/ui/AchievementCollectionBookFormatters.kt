package com.example.roadguideapp.goldhunt.achievements.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntry
import com.example.roadguideapp.goldhunt.achievements.collectionbook.AchievementCollectionBookEntry
import com.example.roadguideapp.goldhunt.achievements.collectionbook.AchievementCollectionBookProgress
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalStatus
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntry
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class AchievementCollectionBookFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatCompletedProgress(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_completed_progress,
            progress.completedCount,
            progress.catalogCount,
        )

    fun formatExplorerLevel(progress: AchievementCollectionBookProgress): String =
        context.getString(R.string.achievement_collection_book_explorer_level, progress.explorerLevel)

    fun formatCompletionPercentage(progress: AchievementCollectionBookProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun formatRareLegendary(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_rare_legendary_progress,
            progress.rareCompletedCount,
            progress.legendaryCompletedCount,
        )

    fun formatBadgesProgress(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_badges_progress,
            progress.badgesUnlockedCount,
            progress.badgesTotalCount,
        )

    fun formatTitlesProgress(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_titles_progress,
            progress.titlesUnlockedCount,
            progress.titlesTotalCount,
        )

    fun formatStoryRelicProgress(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_story_relic_progress,
            progress.storyFragmentsEarned,
            progress.legendaryRelicsEarned,
        )

    fun formatRewardTotals(progress: AchievementCollectionBookProgress): String =
        context.getString(
            R.string.achievement_collection_book_reward_totals,
            progress.creditsEarned,
            progress.xpEarned,
        )

    fun toAchievementUiState(entry: AchievementCollectionBookEntry): AchievementCollectionBookAchievementUiState =
        AchievementCollectionBookAchievementUiState(
            achievementId = entry.achievementId,
            displayName = entry.displayName,
            categoryLabel = entry.category.displayName,
            statusLabel = formatStatus(entry.status),
            rarityLabel = formatRarity(entry.rarityTier),
            progressLabel = formatAchievementProgress(entry),
            rewardHooksLabel = formatRewardHooks(entry),
            minimumLevelLabel = formatMinimumLevel(entry.minimumExplorerLevel, entry.status),
            completionDateLabel = formatDate(entry.completionDateMs, !entry.isCompleted),
            isCompleted = entry.isCompleted,
            isRare = entry.isRare,
            isLegendary = entry.isLegendary,
        )

    fun toBadgeUiState(entry: AchievementBadgeEntry): AchievementCollectionBookBadgeUiState =
        AchievementCollectionBookBadgeUiState(
            badgeKey = entry.badgeKey,
            displayName = entry.title,
            rarityLabel = formatBadgeRarity(entry.rarity.id),
            unlockDateLabel = formatDate(entry.unlockDateMs, entry.isLocked),
            isUnlocked = entry.isUnlocked,
        )

    fun toTitleUiState(entry: AchievementTitleEntry): AchievementCollectionBookTitleUiState =
        AchievementCollectionBookTitleUiState(
            titleKey = entry.titleKey,
            displayName = entry.displayTitle,
            rarityLabel = formatTitleRarity(entry.rarity.id),
            unlockDateLabel = formatDate(entry.unlockDateMs, entry.isLocked),
            isUnlocked = entry.isUnlocked,
            isActive = entry.isActive,
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

    private fun formatRarity(rarity: AchievementRarityTier): String = when (rarity) {
        AchievementRarityTier.COMMON ->
            context.getString(R.string.achievement_collection_book_rarity_common)
        AchievementRarityTier.RARE ->
            context.getString(R.string.achievement_collection_book_rarity_rare)
        AchievementRarityTier.LEGENDARY ->
            context.getString(R.string.achievement_collection_book_rarity_legendary)
    }

    private fun formatBadgeRarity(rarityId: String): String = when (rarityId.lowercase()) {
        "rare" -> context.getString(R.string.achievement_badge_rarity_rare)
        "legendary" -> context.getString(R.string.achievement_badge_rarity_legendary)
        else -> context.getString(R.string.achievement_badge_rarity_common)
    }

    private fun formatTitleRarity(rarityId: String): String = when (rarityId.lowercase()) {
        "rare" -> context.getString(R.string.achievement_title_rarity_rare)
        "legendary" -> context.getString(R.string.achievement_title_rarity_legendary)
        else -> context.getString(R.string.achievement_title_rarity_common)
    }

    private fun formatAchievementProgress(entry: AchievementCollectionBookEntry): String =
        context.getString(
            R.string.achievement_journal_entry_progress,
            entry.currentValue,
            entry.targetValue,
            ExplorerProfileFormatters.formatCompletionPercentage(entry.progressFraction),
        )

    private fun formatRewardHooks(entry: AchievementCollectionBookEntry): String {
        val parts = buildList {
            if (entry.badgeKey != null) {
                add(
                    context.getString(
                        R.string.achievement_collection_book_badge_hook,
                        entry.badgeKey,
                        formatEarned(entry.badgeUnlocked),
                    ),
                )
            }
            if (entry.titleKey != null) {
                add(
                    context.getString(
                        R.string.achievement_collection_book_title_hook,
                        entry.titleKey,
                        formatEarned(entry.titleUnlocked),
                    ),
                )
            }
            if (entry.storyFragmentKey != null) {
                add(
                    context.getString(
                        R.string.achievement_collection_book_story_hook,
                        entry.storyFragmentKey,
                        formatEarned(entry.storyFragmentEarned),
                    ),
                )
            }
            if (entry.legendaryRelicKey != null) {
                add(
                    context.getString(
                        R.string.achievement_collection_book_relic_hook,
                        entry.legendaryRelicKey,
                        formatEarned(entry.legendaryRelicEarned),
                    ),
                )
            }
        }
        return if (parts.isEmpty()) {
            context.getString(R.string.achievement_journal_reward_none)
        } else {
            parts.joinToString(" · ")
        }
    }

    private fun formatEarned(earned: Boolean): String = if (earned) {
        context.getString(R.string.achievement_collection_book_earned)
    } else {
        context.getString(R.string.achievement_collection_book_not_earned)
    }

    private fun formatMinimumLevel(level: Int, status: AchievementJournalStatus): String? {
        if (status != AchievementJournalStatus.LOCKED) return null
        return context.getString(R.string.achievement_journal_minimum_level, level)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.achievement_journal_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }
}
