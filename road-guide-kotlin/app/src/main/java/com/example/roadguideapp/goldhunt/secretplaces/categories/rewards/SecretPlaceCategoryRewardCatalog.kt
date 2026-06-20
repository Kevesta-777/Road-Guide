package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog

/**
 * Read-only reward profiles per [SecretPlaceCategory].
 * Generation and dispatch consume this catalog; no persistence here.
 */
internal object SecretPlaceCategoryRewardCatalog {
    private val profiles: Map<SecretPlaceCategory, SecretPlaceCategoryRewardProfile> =
        SecretPlaceCategoryCatalog.displayOrder.associateWith { category ->
            when (category) {
                SecretPlaceCategory.NATURE_SANCTUARY -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 10,
                    discoveryXp = 70L,
                    completionCredits = 18,
                    completionXp = 90L,
                    flowerBonusSlots = 2,
                    crystalBonusSlots = 1,
                )
                SecretPlaceCategory.SCENIC_VIEWPOINT -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 22,
                    discoveryXp = 80L,
                    completionCredits = 30,
                    completionXp = 110L,
                    creditBonusMultiplier = 1.35,
                    grantsPanoramaBonus = true,
                )
                SecretPlaceCategory.HISTORIC_LANDMARK -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 18,
                    discoveryXp = 95L,
                    completionCredits = 28,
                    completionXp = 130L,
                    grantsStoryFragment = true,
                    grantsAchievement = true,
                )
                SecretPlaceCategory.MYSTERY_ZONE -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 16,
                    discoveryXp = 90L,
                    completionCredits = 24,
                    completionXp = 120L,
                    grantsAchievement = true,
                    treasureClusterBonusSlots = 1,
                )
                SecretPlaceCategory.ANCIENT_RELIC_SITE -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 24,
                    discoveryXp = 110L,
                    completionCredits = 36,
                    completionXp = 150L,
                    crystalBonusSlots = 1,
                    creditBonusMultiplier = 1.15,
                    grantsStoryFragment = true,
                    grantsAchievement = true,
                )
                SecretPlaceCategory.EXPLORER_HIDEOUT -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 20,
                    discoveryXp = 105L,
                    completionCredits = 32,
                    completionXp = 140L,
                    flowerBonusSlots = 1,
                    creditBonusMultiplier = 1.1,
                    grantsAchievement = true,
                    treasureClusterBonusSlots = 1,
                )
                SecretPlaceCategory.MYTHICAL_PLACE -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 32,
                    discoveryXp = 130L,
                    completionCredits = 45,
                    completionXp = 175L,
                    crystalBonusSlots = 2,
                    creditBonusMultiplier = 1.25,
                    grantsPanoramaBonus = true,
                    grantsStoryFragment = true,
                    grantsAchievement = true,
                )
                SecretPlaceCategory.LEGENDARY_SITE -> SecretPlaceCategoryRewardProfile(
                    category = category,
                    discoveryCredits = 45,
                    discoveryXp = 175L,
                    completionCredits = 60,
                    completionXp = 225L,
                    creditBonusMultiplier = 1.5,
                    grantsPanoramaBonus = true,
                    grantsStoryFragment = true,
                    grantsAchievement = true,
                    treasureClusterBonusSlots = 2,
                    grantsLegendaryRelic = true,
                )
            }
        }

    fun profileFor(category: SecretPlaceCategory): SecretPlaceCategoryRewardProfile =
        profiles.getValue(category)
}
