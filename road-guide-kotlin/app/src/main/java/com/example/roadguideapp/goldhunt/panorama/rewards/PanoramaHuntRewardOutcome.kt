package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.rewards.CreditGrant

/**
 * Resolved dispatch package for a panorama hunt completion reward.
 * Hook event ids are stable and idempotent for story, achievement, and relic unlockers.
 */
internal data class PanoramaHuntRewardOutcome(
    val hunt: PanoramaHunt,
    val bundle: PanoramaHuntRewardBundle,
    val creditGrant: CreditGrant,
    val explorerLevel: Int,
    val scaling: PanoramaHuntRewardScalingBreakdown,
) {
    val credits: Int get() = bundle.credits
    val xp: Long get() = bundle.xp

    val baseEventId: String
        get() = "${PanoramaHuntRewardSchema.EventPrefixes.COMPLETION}:${hunt.huntId}"

    fun creditEventId(): String = creditGrant.eventId

    fun xpEventId(): String = "$baseEventId:xp"

    fun storyFragmentEventId(): String? =
        bundle.storyFragmentId?.let {
            "${PanoramaHuntRewardSchema.EventPrefixes.STORY}:${hunt.huntId}:$it"
        }

    fun achievementEventId(): String? =
        bundle.achievementKey?.let {
            "${PanoramaHuntRewardSchema.EventPrefixes.ACHIEVEMENT}:${hunt.huntId}:$it"
        }

    fun legendaryRelicEventId(): String? =
        bundle.legendaryRelicKey?.let {
            "${PanoramaHuntRewardSchema.EventPrefixes.RELIC}:${hunt.huntId}:$it"
        }
}
