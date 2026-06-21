package com.example.roadguideapp.goldhunt.achievements.journal

internal data class AchievementJournalRewardPreview(
    val credits: Int,
    val xp: Long,
    val titleKey: String? = null,
    val legendaryRelicKey: String? = null,
    val badgeKey: String? = null,
    val storyFragmentKey: String? = null,
    val chainUnlockAchievementKeys: List<String> = emptyList(),
    val chainBadgeKeys: List<String> = emptyList(),
    val chainTitleKeys: List<String> = emptyList(),
    val chainLegendaryRelicKeys: List<String> = emptyList(),
    val chainStoryFragmentKeys: List<String> = emptyList(),
) {
    val hasCredits: Boolean get() = credits > 0
    val hasXp: Boolean get() = xp > 0L
    val hasTitle: Boolean get() = !titleKey.isNullOrBlank()
    val hasRelic: Boolean get() = !legendaryRelicKey.isNullOrBlank()
    val hasBadge: Boolean get() = !badgeKey.isNullOrBlank()
    val hasStoryFragment: Boolean get() = !storyFragmentKey.isNullOrBlank()
    val hasChainUnlocks: Boolean get() =
        chainUnlockAchievementKeys.isNotEmpty() ||
            chainBadgeKeys.isNotEmpty() ||
            chainTitleKeys.isNotEmpty() ||
            chainLegendaryRelicKeys.isNotEmpty() ||
            chainStoryFragmentKeys.isNotEmpty()
}
