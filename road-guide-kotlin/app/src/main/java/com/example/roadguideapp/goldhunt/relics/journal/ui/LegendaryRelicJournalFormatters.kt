package com.example.roadguideapp.goldhunt.relics.journal.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalEntry
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalPieceEntry
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalProgress
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalStatus
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryLoreTrigger
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryPreview
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class LegendaryRelicJournalFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatCompletedProgress(progress: LegendaryRelicJournalProgress): String =
        context.getString(
            R.string.legendary_relic_journal_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatCompletionPercentage(progress: LegendaryRelicJournalProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun formatPiecesFoundProgress(progress: LegendaryRelicJournalProgress): String =
        context.getString(
            R.string.legendary_relic_journal_pieces_found_progress,
            progress.piecesFound,
            progress.totalPieces,
        )

    fun formatPiecesMissingProgress(progress: LegendaryRelicJournalProgress): String =
        context.getString(
            R.string.legendary_relic_journal_pieces_missing_progress,
            progress.piecesMissing,
            progress.totalPieces,
        )

    fun formatHiddenDiscoveredProgress(progress: LegendaryRelicJournalProgress): String =
        if (progress.hiddenRelicsCatalogCount <= 0) {
            context.getString(R.string.legendary_relic_journal_hidden_not_applicable)
        } else {
            context.getString(
                R.string.legendary_relic_journal_hidden_discovered_progress,
                progress.hiddenRelicsDiscovered,
                progress.hiddenRelicsCatalogCount,
            )
        }

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun formatXp(value: Long): String = ExplorerProfileFormatters.formatXp(value)

    fun toEntryUiState(entry: LegendaryRelicJournalEntry): LegendaryRelicJournalEntryUiState =
        LegendaryRelicJournalEntryUiState(
            relicId = entry.relicId,
            displayName = entry.displayName,
            description = entry.description,
            categoryLabel = entry.categoryLabel,
            rarityLabel = entry.rarity.displayName,
            statusLabel = formatStatus(entry.status),
            progressLabel = formatPieceProgress(entry.piecesCollected, entry.pieceCount, entry.isHiddenMasked),
            progressFraction = entry.progressFraction,
            piecesFoundLabel = formatPieceCount(entry.piecesCollected, entry.isHiddenMasked),
            piecesMissingLabel = formatPieceCount(entry.piecesMissing, entry.isHiddenMasked),
            completionDateLabel = formatDate(entry.completionDateMs, !entry.isCompleted),
            minimumExplorerLevelLabel = entry.minimumExplorerLevel.toString(),
            rewardsLabel = formatEntryCredits(entry),
            xpLabel = formatEntryXp(entry),
            badgeLabel = entry.rewardPreview.badgeKey?.let { formatHookLabel(it, entry.isCompleted) },
            titleLabel = entry.rewardPreview.titleKey?.let { formatHookLabel(it, entry.isCompleted) },
            powerLabel = entry.rewardPreview.powerKey?.let { formatHookLabel(it, entry.isCompleted) },
            storyFragmentLabel = entry.rewardPreview.storyFragmentKey?.let {
                formatHookLabel(it, entry.isCompleted)
            },
            storyPreview = entry.storyPreview.takeIf { it.hasStoryContent }?.let { toStoryUiState(it) },
            chainUnlocksLabel = formatChainUnlocks(entry),
            rewardsGrantedLabel = formatRewardsGranted(entry),
            pieces = entry.pieces.map { toPieceUiState(it) },
            isCompleted = entry.isCompleted,
            isInProgress = entry.isInProgress,
            isMissing = entry.isMissing,
            isLocked = entry.isLocked,
            isHiddenMasked = entry.isHiddenMasked,
        )

    private fun toPieceUiState(piece: LegendaryRelicJournalPieceEntry): LegendaryRelicJournalPieceUiState =
        LegendaryRelicJournalPieceUiState(
            pieceNumber = piece.pieceNumber,
            statusLabel = when {
                piece.masked -> context.getString(R.string.legendary_relic_journal_piece_hidden)
                piece.discovered -> context.getString(R.string.legendary_relic_journal_piece_found)
                else -> context.getString(R.string.legendary_relic_journal_piece_missing)
            },
            discoveryDateLabel = formatDate(piece.discoveryDateMs, piece.isMissing || piece.masked),
            sourceLabel = if (piece.masked) {
                context.getString(R.string.legendary_relic_journal_not_recorded)
            } else {
                formatSourceType(piece.sourceType.id)
            },
            isMissing = piece.isMissing,
            isMasked = piece.masked,
        )

    private fun formatStatus(status: LegendaryRelicJournalStatus): String = when (status) {
        LegendaryRelicJournalStatus.COMPLETED ->
            context.getString(R.string.legendary_relic_journal_status_completed)
        LegendaryRelicJournalStatus.IN_PROGRESS ->
            context.getString(R.string.legendary_relic_journal_status_in_progress)
        LegendaryRelicJournalStatus.MISSING ->
            context.getString(R.string.legendary_relic_journal_status_missing)
        LegendaryRelicJournalStatus.LOCKED ->
            context.getString(R.string.legendary_relic_journal_status_locked)
        LegendaryRelicJournalStatus.HIDDEN_MASKED ->
            context.getString(R.string.legendary_relic_journal_status_hidden)
    }

    private fun formatPieceProgress(collected: Int, total: Int, masked: Boolean): String =
        if (masked) {
            context.getString(R.string.legendary_relic_journal_not_recorded)
        } else {
            context.getString(
                R.string.legendary_relic_journal_piece_progress,
                collected,
                total,
            )
        }

    private fun formatPieceCount(count: Int, masked: Boolean): String =
        if (masked) {
            context.getString(R.string.legendary_relic_journal_not_recorded)
        } else {
            count.toString()
        }

    private fun formatEntryCredits(entry: LegendaryRelicJournalEntry): String =
        if (entry.isHiddenMasked) {
            context.getString(R.string.legendary_relic_journal_not_recorded)
        } else {
            formatCredits(entry.rewardPreview.credits)
        }

    private fun formatEntryXp(entry: LegendaryRelicJournalEntry): String =
        if (entry.isHiddenMasked) {
            context.getString(R.string.legendary_relic_journal_not_recorded)
        } else {
            formatXp(entry.rewardPreview.xp)
        }

    private fun formatRewardsGranted(entry: LegendaryRelicJournalEntry): String = when {
        entry.isHiddenMasked -> context.getString(R.string.legendary_relic_journal_not_recorded)
        entry.rewardPreview.rewardsGranted ->
            context.getString(R.string.legendary_relic_journal_rewards_granted)
        entry.isCompleted ->
            context.getString(R.string.legendary_relic_journal_rewards_pending)
        else -> context.getString(R.string.legendary_relic_journal_rewards_locked)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.legendary_relic_journal_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatSourceType(sourceTypeId: String): String = when (sourceTypeId) {
        "secretPlace" -> context.getString(R.string.legendary_relic_journal_source_secret_place)
        "treasureCluster" -> context.getString(R.string.legendary_relic_journal_source_treasure_cluster)
        "storyFragment" -> context.getString(R.string.legendary_relic_journal_source_story_fragment)
        "panoramaHunt" -> context.getString(R.string.legendary_relic_journal_source_panorama_hunt)
        "seasonalEvent" -> context.getString(R.string.legendary_relic_journal_source_seasonal_event)
        "achievement" -> context.getString(R.string.legendary_relic_journal_source_achievement)
        "radarDiscovery" -> context.getString(R.string.legendary_relic_journal_source_radar_discovery)
        else -> sourceTypeId
    }

    private fun formatHookLabel(key: String, unlocked: Boolean): String =
        if (unlocked) {
            context.getString(R.string.legendary_relic_journal_hook_unlocked, key)
        } else {
            context.getString(R.string.legendary_relic_journal_hook_locked, key)
        }

    private fun toStoryUiState(story: LegendaryRelicStoryPreview): LegendaryRelicJournalStoryUiState =
        LegendaryRelicJournalStoryUiState(
            chapters = story.chapters.map { chapter ->
                LegendaryRelicJournalStoryChapterUiState(
                    chapterNumber = chapter.chapterNumber,
                    title = chapter.title,
                    body = chapter.body,
                    statusLabel = formatStoryStatus(chapter.unlocked),
                    unlockDateLabel = formatDate(chapter.unlockDateMs, !chapter.unlocked),
                    unlocked = chapter.unlocked,
                )
            },
            loreEntries = story.loreEntries.map { lore ->
                LegendaryRelicJournalStoryLoreUiState(
                    title = lore.title,
                    body = lore.body,
                    triggerLabel = formatLoreTrigger(lore.trigger),
                    statusLabel = formatStoryStatus(lore.unlocked),
                    unlockDateLabel = formatDate(lore.unlockDateMs, !lore.unlocked),
                    unlocked = lore.unlocked,
                )
            },
            completionStory = story.completionStory?.let { completion ->
                LegendaryRelicJournalStoryCompletionUiState(
                    title = completion.title,
                    body = completion.body,
                    storyFragmentLabel = formatHookLabel(
                        completion.storyFragmentKey,
                        completion.unlocked,
                    ),
                    statusLabel = formatStoryStatus(completion.unlocked),
                    unlockDateLabel = formatDate(completion.unlockDateMs, !completion.unlocked),
                    unlocked = completion.unlocked,
                )
            },
            progressLabel = context.getString(
                R.string.legendary_relic_story_progress,
                story.unlockedFragmentCount,
                story.totalFragmentCount,
            ),
        )

    private fun formatStoryStatus(unlocked: Boolean): String =
        if (unlocked) {
            context.getString(R.string.legendary_relic_story_status_unlocked)
        } else {
            context.getString(R.string.legendary_relic_story_status_locked)
        }

    private fun formatChainUnlocks(entry: LegendaryRelicJournalEntry): String? {
        val preview = entry.rewardPreview
        if (!preview.hasChainUnlocks || entry.isHiddenMasked) return null
        val labels = buildList {
            preview.chainUnlockRelicKeys.forEach { key ->
                add(context.getString(R.string.legendary_relic_journal_chain_unlock_relic, key))
            }
            preview.chainUnlockStoryFragmentKeys.forEach { key ->
                add(context.getString(R.string.legendary_relic_journal_chain_unlock_story, key))
            }
            preview.chainUnlockAchievementKeys.forEach { key ->
                add(context.getString(R.string.legendary_relic_journal_chain_unlock_achievement, key))
            }
            preview.chainUnlockBadgeKeys.forEach { key ->
                add(context.getString(R.string.legendary_relic_journal_chain_unlock_badge, key))
            }
            preview.chainUnlockTitleKeys.forEach { key ->
                add(context.getString(R.string.legendary_relic_journal_chain_unlock_title, key))
            }
        }
        return labels.joinToString(separator = "\n")
    }

    private fun formatLoreTrigger(trigger: LegendaryRelicStoryLoreTrigger): String = when (trigger) {
        LegendaryRelicStoryLoreTrigger.FOUNDATION ->
            context.getString(R.string.legendary_relic_story_lore_trigger_foundation)
        LegendaryRelicStoryLoreTrigger.REVEAL ->
            context.getString(R.string.legendary_relic_story_lore_trigger_reveal)
        LegendaryRelicStoryLoreTrigger.COMPLETION ->
            context.getString(R.string.legendary_relic_story_lore_trigger_completion)
    }
}
