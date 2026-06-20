package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalRewardPreview

internal data class RelicHuntChainRewardPreview(
    val chainUnlockRelicKeys: List<String> = emptyList(),
    val chainUnlockAchievementKeys: List<String> = emptyList(),
    val chainUnlockBadgeKeys: List<String> = emptyList(),
    val chainUnlockTitleKeys: List<String> = emptyList(),
    val chainUnlockStoryFragmentKeys: List<String> = emptyList(),
) {
    val hasChainUnlocks: Boolean
        get() = chainUnlockRelicKeys.isNotEmpty() ||
            chainUnlockAchievementKeys.isNotEmpty() ||
            chainUnlockBadgeKeys.isNotEmpty() ||
            chainUnlockTitleKeys.isNotEmpty() ||
            chainUnlockStoryFragmentKeys.isNotEmpty()
}

internal object RelicHuntChainRewardPreviewMapper {
    fun previewFor(sourceRelicId: String): RelicHuntChainRewardPreview {
        val unlocks = RelicHuntChainCatalog.rewardsFor(sourceRelicId)
        if (unlocks.isEmpty()) return RelicHuntChainRewardPreview()
        return RelicHuntChainRewardPreview(
            chainUnlockRelicKeys = unlocks.filterIsInstance<RelicHuntChainUnlock.LegendaryRelic>()
                .map { it.relicKey },
            chainUnlockAchievementKeys = unlocks.filterIsInstance<RelicHuntChainUnlock.Achievement>()
                .map { it.achievementKey },
            chainUnlockBadgeKeys = unlocks.filterIsInstance<RelicHuntChainUnlock.Badge>()
                .map { it.badgeKey },
            chainUnlockTitleKeys = unlocks.filterIsInstance<RelicHuntChainUnlock.Title>()
                .map { it.titleKey },
            chainUnlockStoryFragmentKeys = unlocks.filterIsInstance<RelicHuntChainUnlock.StoryFragment>()
                .map { it.storyFragmentKey },
        )
    }

    fun appendTo(
        preview: LegendaryRelicJournalRewardPreview,
        sourceRelicId: String,
    ): LegendaryRelicJournalRewardPreview {
        val chainPreview = previewFor(sourceRelicId)
        if (!chainPreview.hasChainUnlocks) return preview
        return preview.copy(
            chainUnlockRelicKeys = chainPreview.chainUnlockRelicKeys,
            chainUnlockAchievementKeys = chainPreview.chainUnlockAchievementKeys,
            chainUnlockBadgeKeys = chainPreview.chainUnlockBadgeKeys,
            chainUnlockTitleKeys = chainPreview.chainUnlockTitleKeys,
            chainUnlockStoryFragmentKeys = chainPreview.chainUnlockStoryFragmentKeys,
        )
    }
}
