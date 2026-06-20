package com.example.roadguideapp.goldhunt.achievements.collectionbook

internal data class AchievementHookEarnedFlags(
    val storyFragmentEarned: Boolean = false,
    val legendaryRelicEarned: Boolean = false,
    val titleEarned: Boolean = false,
    val badgeEarned: Boolean = false,
) {
    companion object {
        val EMPTY = AchievementHookEarnedFlags()
    }
}
