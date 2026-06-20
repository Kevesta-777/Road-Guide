package com.example.roadguideapp.goldhunt.relics.showcase.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseAchievementLink
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseBadgeHighlight
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseEntry
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseProgress
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseSchema
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseStoryHighlight
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseTitleHighlight
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class LegendaryRelicShowcaseFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatCompletedProgress(progress: LegendaryRelicShowcaseProgress): String =
        context.getString(
            R.string.legendary_relic_showcase_completed_progress,
            progress.completedCount,
            progress.trackableCount,
        )

    fun formatCompletionPercentage(progress: LegendaryRelicShowcaseProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun formatBadgesEarned(progress: LegendaryRelicShowcaseProgress): String =
        context.getString(
            R.string.legendary_relic_showcase_badges_earned,
            progress.badgesEarned,
            progress.completedCount,
        )

    fun formatTitlesEarned(progress: LegendaryRelicShowcaseProgress): String =
        context.getString(
            R.string.legendary_relic_showcase_titles_earned,
            progress.titlesEarned,
            progress.completedCount,
        )

    fun formatAchievementLinks(progress: LegendaryRelicShowcaseProgress): String =
        context.getString(
            R.string.legendary_relic_showcase_achievement_links,
            progress.linkedAchievementsCompleted,
            progress.linkedAchievementsTotal,
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun formatXp(value: Long): String = ExplorerProfileFormatters.formatXp(value)

    fun toEntryUiState(entry: LegendaryRelicShowcaseEntry): LegendaryRelicShowcaseEntryUiState =
        LegendaryRelicShowcaseEntryUiState(
            relicId = entry.relicId,
            displayName = entry.displayName,
            description = entry.description,
            categoryLabel = entry.categoryLabel,
            rarityLabel = formatRelicRarity(entry.rarity),
            completionDateLabel = formatDate(entry.completionDateMs),
            creditsLabel = formatCredits(entry.creditsEarned),
            xpLabel = formatXp(entry.xpEarned),
            badge = entry.badge?.let { toBadgeUiState(it) },
            title = entry.title?.let { toTitleUiState(it) },
            achievementLinks = entry.achievementLinks.map { toAchievementLinkUiState(it) },
            story = entry.story?.let { toStoryUiState(it) },
            rewardsGrantedLabel = if (entry.rewardsGranted) {
                context.getString(R.string.legendary_relic_showcase_rewards_granted)
            } else {
                context.getString(R.string.legendary_relic_showcase_rewards_pending)
            },
        )

    fun toBadgeUiState(badge: LegendaryRelicShowcaseBadgeHighlight): LegendaryRelicShowcaseBadgeUiState =
        LegendaryRelicShowcaseBadgeUiState(
            badgeKey = badge.badgeKey,
            title = badge.title,
            iconLabel = context.getString(R.string.legendary_relic_showcase_icon_label, badge.iconKey),
            rarityLabel = formatBadgeRarity(badge.rarity),
            unlockDateLabel = formatUnlockDate(badge.unlockDateMs, !badge.earned),
            earned = badge.earned,
        )

    fun toTitleUiState(title: LegendaryRelicShowcaseTitleHighlight): LegendaryRelicShowcaseTitleUiState =
        LegendaryRelicShowcaseTitleUiState(
            titleKey = title.titleKey,
            displayTitle = title.displayTitle,
            rarityLabel = formatTitleRarity(title.rarity),
            unlockDateLabel = formatUnlockDate(title.unlockDateMs, !title.earned),
            earned = title.earned,
            activeLabel = if (title.isActive) {
                context.getString(R.string.legendary_relic_showcase_title_active)
            } else {
                null
            },
        )

    fun toStoryUiState(story: LegendaryRelicShowcaseStoryHighlight): LegendaryRelicShowcaseStoryUiState =
        LegendaryRelicShowcaseStoryUiState(
            title = story.title,
            body = story.body,
            storyFragmentLabel = story.storyFragmentKey,
            chaptersProgressLabel = context.getString(
                R.string.legendary_relic_showcase_story_chapters_progress,
                story.chaptersUnlocked,
                story.chaptersTotal,
            ),
            loreProgressLabel = context.getString(
                R.string.legendary_relic_showcase_story_lore_progress,
                story.loreUnlocked,
                story.loreTotal,
            ),
            completionStatusLabel = if (story.completionUnlocked) {
                context.getString(R.string.legendary_relic_showcase_story_completion_unlocked)
            } else {
                context.getString(R.string.legendary_relic_showcase_story_completion_locked)
            },
            unlockDateLabel = formatDate(story.unlockDateMs, unavailable = !story.completionUnlocked),
        )

    private fun toAchievementLinkUiState(
        link: LegendaryRelicShowcaseAchievementLink,
    ): LegendaryRelicShowcaseAchievementLinkUiState =
        LegendaryRelicShowcaseAchievementLinkUiState(
            achievementId = link.achievementId,
            title = link.title,
            linkTypeLabel = formatAchievementLinkType(link.linkType),
            statusLabel = if (link.completed) {
                context.getString(R.string.legendary_relic_showcase_achievement_completed)
            } else {
                context.getString(R.string.legendary_relic_showcase_achievement_incomplete)
            },
            completionDateLabel = formatDate(link.completionDateMs, unavailable = !link.completed),
            isCompleted = link.completed,
        )

    private fun formatAchievementLinkType(linkType: String): String = when (linkType) {
        LegendaryRelicShowcaseSchema.AchievementLinkTypes.GRANTS_RELIC ->
            context.getString(R.string.legendary_relic_showcase_link_grants_relic)
        LegendaryRelicShowcaseSchema.AchievementLinkTypes.PIECE_SOURCE ->
            context.getString(R.string.legendary_relic_showcase_link_piece_source)
        LegendaryRelicShowcaseSchema.AchievementLinkTypes.CATEGORY_DISCOVERY ->
            context.getString(R.string.legendary_relic_showcase_link_category_discovery)
        else -> linkType
    }

    private fun formatRelicRarity(rarity: RelicRarity): String = rarity.displayName

    private fun formatBadgeRarity(rarity: AchievementBadgeRarity): String = when (rarity) {
        AchievementBadgeRarity.COMMON ->
            context.getString(R.string.achievement_badge_rarity_common)
        AchievementBadgeRarity.RARE ->
            context.getString(R.string.achievement_badge_rarity_rare)
        AchievementBadgeRarity.LEGENDARY ->
            context.getString(R.string.achievement_badge_rarity_legendary)
    }

    private fun formatTitleRarity(rarity: AchievementTitleRarity): String = when (rarity) {
        AchievementTitleRarity.COMMON ->
            context.getString(R.string.achievement_title_rarity_common)
        AchievementTitleRarity.RARE ->
            context.getString(R.string.achievement_title_rarity_rare)
        AchievementTitleRarity.LEGENDARY ->
            context.getString(R.string.achievement_title_rarity_legendary)
    }

    private fun formatDate(timestampMs: Long?, unavailable: Boolean = false): String {
        if (unavailable || timestampMs == null || timestampMs <= 0L) {
            return context.getString(R.string.legendary_relic_showcase_not_recorded)
        }
        return dateFormat.format(Date(timestampMs))
    }

    private fun formatUnlockDate(unlockDateMs: Long?, locked: Boolean): String =
        formatDate(unlockDateMs, unavailable = locked)
}
