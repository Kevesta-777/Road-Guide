package com.example.roadguideapp.goldhunt.relics.journal

internal data class LegendaryRelicJournalRewardPreview(
    val credits: Int = 0,
    val xp: Long = 0L,
    val badgeKey: String? = null,
    val titleKey: String? = null,
    val powerKey: String? = null,
    val storyFragmentKey: String? = null,
    val chainUnlockRelicKeys: List<String> = emptyList(),
    val chainUnlockAchievementKeys: List<String> = emptyList(),
    val chainUnlockBadgeKeys: List<String> = emptyList(),
    val chainUnlockTitleKeys: List<String> = emptyList(),
    val chainUnlockStoryFragmentKeys: List<String> = emptyList(),
    val rewardsGranted: Boolean = false,
) {
    val hasStoryFragment: Boolean get() = !storyFragmentKey.isNullOrBlank()

    val hasChainUnlocks: Boolean
        get() = chainUnlockRelicKeys.isNotEmpty() ||
            chainUnlockAchievementKeys.isNotEmpty() ||
            chainUnlockBadgeKeys.isNotEmpty() ||
            chainUnlockTitleKeys.isNotEmpty() ||
            chainUnlockStoryFragmentKeys.isNotEmpty()
}
