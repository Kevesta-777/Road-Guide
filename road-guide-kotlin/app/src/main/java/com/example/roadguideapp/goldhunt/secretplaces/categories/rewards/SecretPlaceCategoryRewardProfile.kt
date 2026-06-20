package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Offline reward profile for a [SecretPlaceCategory] family.
 * Base values are scaled by instance [com.example.roadguideapp.goldhunt.secretplaces.SecretPlace.effectiveRewardMultiplier].
 */
internal data class SecretPlaceCategoryRewardProfile(
    val category: SecretPlaceCategory,
    val discoveryCredits: Int,
    val discoveryXp: Long,
    val completionCredits: Int,
    val completionXp: Long,
    val flowerBonusSlots: Int = 0,
    val crystalBonusSlots: Int = 0,
    val creditBonusMultiplier: Double = 1.0,
    val grantsPanoramaBonus: Boolean = false,
    val grantsStoryFragment: Boolean = false,
    val grantsAchievement: Boolean = false,
    val treasureClusterBonusSlots: Int = 0,
    val grantsLegendaryRelic: Boolean = false,
) {
    val effects: Set<SecretPlaceCategoryRewardEffect>
        get() = buildSet {
            add(SecretPlaceCategoryRewardEffect.CREDITS)
            add(SecretPlaceCategoryRewardEffect.XP)
            if (flowerBonusSlots > 0) add(SecretPlaceCategoryRewardEffect.FLOWER_BONUS)
            if (crystalBonusSlots > 0) add(SecretPlaceCategoryRewardEffect.CRYSTAL_BONUS)
            if (creditBonusMultiplier > 1.0) add(SecretPlaceCategoryRewardEffect.CREDITS)
            if (grantsPanoramaBonus) add(SecretPlaceCategoryRewardEffect.PANORAMA_BONUS)
            if (grantsStoryFragment) add(SecretPlaceCategoryRewardEffect.STORY_FRAGMENT)
            if (grantsAchievement) add(SecretPlaceCategoryRewardEffect.ACHIEVEMENT)
            if (treasureClusterBonusSlots > 0) add(SecretPlaceCategoryRewardEffect.TREASURE_CLUSTER)
            if (grantsLegendaryRelic) add(SecretPlaceCategoryRewardEffect.LEGENDARY_RELIC)
        }

    init {
        require(discoveryCredits >= 0) { "discoveryCredits must not be negative" }
        require(completionCredits >= 0) { "completionCredits must not be negative" }
        require(discoveryXp >= 0L) { "discoveryXp must not be negative" }
        require(completionXp >= 0L) { "completionXp must not be negative" }
        require(flowerBonusSlots >= 0) { "flowerBonusSlots must not be negative" }
        require(crystalBonusSlots >= 0) { "crystalBonusSlots must not be negative" }
        require(treasureClusterBonusSlots >= 0) { "treasureClusterBonusSlots must not be negative" }
        require(creditBonusMultiplier > 0.0) { "creditBonusMultiplier must be positive" }
    }
}
