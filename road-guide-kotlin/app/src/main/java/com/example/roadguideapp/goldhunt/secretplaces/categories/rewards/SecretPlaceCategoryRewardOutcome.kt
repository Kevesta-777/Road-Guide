package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace

/**
 * Resolved dispatch package for a secret-place discovery or completion event.
 *
 * [bundle] holds scaled values; [creditGrant] is ready for [com.example.roadguideapp.goldhunt.rewards.RewardDispatcher]
 * consumers. Hook event ids are stable and idempotent for future unlockers (story, panorama, relic, etc.).
 */
internal data class SecretPlaceCategoryRewardOutcome(
    val phase: SecretPlaceCategoryRewardPhase,
    val place: SecretPlace,
    val bundle: SecretPlaceCategoryRewardBundle,
    val creditGrant: CreditGrant?,
) {
    val secretPlaceId: String get() = place.secretPlaceId
    val category get() = bundle.category
    val credits: Int get() = bundle.credits
    val xp: Long get() = bundle.xp
    val effects get() = bundle.effects

    val baseEventId: String
        get() = "secret_place_category:${phase.id}:$secretPlaceId"

    fun creditEventId(): String? = creditGrant?.eventId

    fun xpEventId(): String? =
        if (bundle.xp > 0L) "$baseEventId:xp" else null

    fun flowerBonusEventId(): String? =
        if (bundle.hasFlowerBonus) "$baseEventId:flower_bonus" else null

    fun crystalBonusEventId(): String? =
        if (bundle.hasCrystalBonus) "$baseEventId:crystal_bonus" else null

    fun panoramaBonusEventId(): String? =
        bundle.panoramaBonusKey?.let { "$baseEventId:panorama:$it" }

    fun storyFragmentEventId(): String? =
        bundle.storyFragmentId?.let { "$baseEventId:story:$it" }

    fun achievementEventId(): String? =
        bundle.achievementKey?.let { "$baseEventId:achievement:$it" }

    fun treasureClusterBonusEventId(): String? =
        if (bundle.hasTreasureClusterBonus) "$baseEventId:treasure_cluster" else null

    fun legendaryRelicEventId(): String? =
        bundle.legendaryRelicKey?.let { "$baseEventId:relic:$it" }
}
