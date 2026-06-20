package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategorySchema
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal object SecretPlaceCategoryRewards {
    fun discoveryBundle(place: SecretPlace): SecretPlaceCategoryRewardBundle {
        val profile = SecretPlaceCategoryRewardCatalog.profileFor(place.category)
        return buildBundle(
            place = place,
            profile = profile,
            baseCredits = profile.discoveryCredits,
            baseXp = profile.discoveryXp,
        )
    }

    fun completionBundle(place: SecretPlace): SecretPlaceCategoryRewardBundle {
        val profile = SecretPlaceCategoryRewardCatalog.profileFor(place.category)
        return buildBundle(
            place = place,
            profile = profile,
            baseCredits = profile.completionCredits,
            baseXp = profile.completionXp,
        )
    }

    private fun buildBundle(
        place: SecretPlace,
        profile: SecretPlaceCategoryRewardProfile,
        baseCredits: Int,
        baseXp: Long,
    ): SecretPlaceCategoryRewardBundle {
        val multiplier = place.effectiveRewardMultiplier * profile.creditBonusMultiplier
        return SecretPlaceCategoryRewardBundle(
            category = place.category,
            credits = scaleCredits(baseCredits, multiplier),
            xp = scaleXp(baseXp, place.effectiveRewardMultiplier),
            flowerBonusSlots = scaleSlots(profile.flowerBonusSlots, place.effectiveRewardMultiplier),
            crystalBonusSlots = scaleSlots(profile.crystalBonusSlots, place.effectiveRewardMultiplier),
            creditBonusMultiplier = profile.creditBonusMultiplier,
            panoramaBonusKey = resolvePanoramaKey(place, profile),
            storyFragmentId = resolveStoryFragmentId(place, profile),
            achievementKey = resolveAchievementKey(place, profile),
            treasureClusterBonusSlots = scaleSlots(
                profile.treasureClusterBonusSlots,
                place.effectiveRewardMultiplier,
            ),
            legendaryRelicKey = resolveLegendaryRelicKey(place, profile),
            effects = profile.effects,
        )
    }

    private fun resolvePanoramaKey(
        place: SecretPlace,
        profile: SecretPlaceCategoryRewardProfile,
    ): String? = if (profile.grantsPanoramaBonus) {
        place.category.panoramaHuntKey
            ?: SecretPlaceCategorySchema.PanoramaHuntKeys.forCategory(place.category)
    } else {
        null
    }

    private fun resolveStoryFragmentId(
        place: SecretPlace,
        profile: SecretPlaceCategoryRewardProfile,
    ): String? = if (profile.grantsStoryFragment) {
        place.resolvedStoryFragmentId
            ?: SecretPlaceCategorySchema.StoryFragmentKeys.forCategory(place.category)
    } else {
        null
    }

    private fun resolveAchievementKey(
        place: SecretPlace,
        profile: SecretPlaceCategoryRewardProfile,
    ): String? = if (profile.grantsAchievement) {
        place.resolvedAchievementKey
            ?: SecretPlaceCategorySchema.AchievementKeys.firstVisit(place.category)
    } else {
        null
    }

    private fun resolveLegendaryRelicKey(
        place: SecretPlace,
        profile: SecretPlaceCategoryRewardProfile,
    ): String? = if (profile.grantsLegendaryRelic) {
        place.resolvedLegendaryRelicKey
            ?: SecretPlaceCategorySchema.LegendaryRelicKeys.forCategory(place.category)
    } else {
        null
    }

    private fun scaleCredits(base: Int, multiplier: Double): Int =
        (base * multiplier).roundToInt().coerceAtLeast(0)

    private fun scaleXp(base: Long, multiplier: Double): Long =
        (base * multiplier).roundToLong().coerceAtLeast(0L)

    private fun scaleSlots(base: Int, multiplier: Double): Int =
        if (base <= 0) 0 else (base * multiplier).roundToInt().coerceAtLeast(1)
}
