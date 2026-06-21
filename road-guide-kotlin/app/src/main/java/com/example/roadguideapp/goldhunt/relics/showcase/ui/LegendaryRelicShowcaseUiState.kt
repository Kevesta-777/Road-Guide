package com.example.roadguideapp.goldhunt.relics.showcase.ui

import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcase
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseBadgeHighlight
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseEntry
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseProgress
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseTitleHighlight

internal data class LegendaryRelicShowcaseAchievementLinkUiState(
    val achievementId: String,
    val title: String,
    val linkTypeLabel: String,
    val statusLabel: String,
    val completionDateLabel: String,
    val isCompleted: Boolean,
)

internal data class LegendaryRelicShowcaseBadgeUiState(
    val badgeKey: String,
    val title: String,
    val iconLabel: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val earned: Boolean,
)

internal data class LegendaryRelicShowcaseTitleUiState(
    val titleKey: String,
    val displayTitle: String,
    val rarityLabel: String,
    val unlockDateLabel: String,
    val earned: Boolean,
    val activeLabel: String?,
)

internal data class LegendaryRelicShowcaseStoryUiState(
    val title: String,
    val body: String,
    val storyFragmentLabel: String,
    val chaptersProgressLabel: String,
    val loreProgressLabel: String,
    val completionStatusLabel: String,
    val unlockDateLabel: String,
)

internal data class LegendaryRelicShowcaseEntryUiState(
    val relicId: String,
    val displayName: String,
    val description: String,
    val categoryLabel: String,
    val rarityLabel: String,
    val completionDateLabel: String,
    val creditsLabel: String,
    val xpLabel: String,
    val badge: LegendaryRelicShowcaseBadgeUiState?,
    val title: LegendaryRelicShowcaseTitleUiState?,
    val achievementLinks: List<LegendaryRelicShowcaseAchievementLinkUiState>,
    val story: LegendaryRelicShowcaseStoryUiState?,
    val rewardsGrantedLabel: String,
)

internal data class LegendaryRelicShowcaseUiState(
    val completedProgressLabel: String,
    val completionPercentageLabel: String,
    val badgesEarnedLabel: String,
    val titlesEarnedLabel: String,
    val achievementLinksLabel: String,
    val completionProgressFraction: Float,
    val totalRewardsLabel: String,
    val totalXpLabel: String,
    val entries: List<LegendaryRelicShowcaseEntryUiState>,
    val badgeHighlights: List<LegendaryRelicShowcaseBadgeUiState>,
    val titleHighlights: List<LegendaryRelicShowcaseTitleUiState>,
) {
    companion object {
        val Empty = LegendaryRelicShowcaseUiState(
            completedProgressLabel = "",
            completionPercentageLabel = "",
            badgesEarnedLabel = "",
            titlesEarnedLabel = "",
            achievementLinksLabel = "",
            completionProgressFraction = 0f,
            totalRewardsLabel = "",
            totalXpLabel = "",
            entries = emptyList(),
            badgeHighlights = emptyList(),
            titleHighlights = emptyList(),
        )

        fun from(showcase: LegendaryRelicShowcase, formatters: LegendaryRelicShowcaseFormatters): LegendaryRelicShowcaseUiState =
            from(
                entries = showcase.entries,
                badgeHighlights = showcase.badgeHighlights,
                titleHighlights = showcase.titleHighlights,
                progress = showcase.progress,
                formatters = formatters,
            )

        fun from(
            entries: List<LegendaryRelicShowcaseEntry>,
            badgeHighlights: List<LegendaryRelicShowcaseBadgeHighlight>,
            titleHighlights: List<LegendaryRelicShowcaseTitleHighlight>,
            progress: LegendaryRelicShowcaseProgress,
            formatters: LegendaryRelicShowcaseFormatters,
        ): LegendaryRelicShowcaseUiState = LegendaryRelicShowcaseUiState(
            completedProgressLabel = formatters.formatCompletedProgress(progress),
            completionPercentageLabel = formatters.formatCompletionPercentage(progress),
            badgesEarnedLabel = formatters.formatBadgesEarned(progress),
            titlesEarnedLabel = formatters.formatTitlesEarned(progress),
            achievementLinksLabel = formatters.formatAchievementLinks(progress),
            completionProgressFraction = progress.completionFraction,
            totalRewardsLabel = formatters.formatCredits(progress.totalCreditsEarned),
            totalXpLabel = formatters.formatXp(progress.totalXpEarned),
            entries = entries.map { formatters.toEntryUiState(it) },
            badgeHighlights = badgeHighlights.map { formatters.toBadgeUiState(it) },
            titleHighlights = titleHighlights.map { formatters.toTitleUiState(it) },
        )
    }
}
