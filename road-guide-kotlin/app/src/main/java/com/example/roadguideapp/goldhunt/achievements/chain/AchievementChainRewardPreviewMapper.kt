package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRewardPreview

internal object AchievementChainRewardPreviewMapper {
    fun appendTo(
        preview: AchievementJournalRewardPreview,
        sourceAchievementKey: String,
    ): AchievementJournalRewardPreview {
        val unlocks = AchievementChainCatalog.rewardsFor(sourceAchievementKey)
        if (unlocks.isEmpty()) return preview
        return preview.copy(
            chainUnlockAchievementKeys = unlocks.filterIsInstance<AchievementChainUnlock.Achievement>()
                .map { it.achievementKey },
            chainBadgeKeys = unlocks.filterIsInstance<AchievementChainUnlock.Badge>()
                .map { it.badgeKey },
            chainTitleKeys = unlocks.filterIsInstance<AchievementChainUnlock.Title>()
                .map { it.titleKey },
            chainLegendaryRelicKeys = unlocks.filterIsInstance<AchievementChainUnlock.LegendaryRelic>()
                .map { it.relicKey },
            chainStoryFragmentKeys = unlocks.filterIsInstance<AchievementChainUnlock.StoryFragment>()
                .map { it.storyFragmentKey },
        )
    }
}
