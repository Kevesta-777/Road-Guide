package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

internal data class PanoramaHuntRewardBundle(
    val credits: Int,
    val xp: Long,
    val rewardMultiplier: Double,
    val huntType: PanoramaHuntType,
    val storyFragmentId: String? = null,
    val achievementKey: String? = null,
    val legendaryRelicKey: String? = null,
    val achievementProgressIncrement: Int = 1,
) {
    val hasStoryFragment: Boolean get() = !storyFragmentId.isNullOrBlank()
    val hasAchievement: Boolean get() = !achievementKey.isNullOrBlank()
    val hasLegendaryRelic: Boolean get() = !legendaryRelicKey.isNullOrBlank()
}
