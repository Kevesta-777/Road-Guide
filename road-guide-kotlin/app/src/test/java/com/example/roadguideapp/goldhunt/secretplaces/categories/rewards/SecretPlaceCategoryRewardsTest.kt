package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlaceRarity
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretPlaceCategoryRewardsTest {
    @Test
    fun natureSanctuary_grantsFlowerAndCrystalBonuses() {
        val bundle = SecretPlaceCategoryRewards.discoveryBundle(
            samplePlace(SecretPlaceCategory.NATURE_SANCTUARY),
        )
        assertTrue(bundle.hasFlowerBonus)
        assertTrue(bundle.hasCrystalBonus)
        assertNull(bundle.panoramaBonusKey)
        assertNull(bundle.storyFragmentId)
    }

    @Test
    fun scenicViewpoint_grantsCreditsAndPanoramaBonus() {
        val bundle = SecretPlaceCategoryRewards.completionBundle(
            samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT),
        )
        assertTrue(bundle.credits > 0)
        assertNotNull(bundle.panoramaBonusKey)
        assertTrue(bundle.hasPanoramaBonus)
        assertTrue(bundle.creditBonusMultiplier > 1.0)
    }

    @Test
    fun historicLandmark_grantsStoryFragmentAndAchievement() {
        val bundle = SecretPlaceCategoryRewards.discoveryBundle(
            samplePlace(SecretPlaceCategory.HISTORIC_LANDMARK),
        )
        assertNotNull(bundle.achievementKey)
        assertNotNull(bundle.storyFragmentId)
        assertTrue(bundle.hasAchievement)
        assertTrue(bundle.hasStoryFragment)
    }

    @Test
    fun ancientRelicSite_grantsStoryFragment() {
        val bundle = SecretPlaceCategoryRewards.completionBundle(
            samplePlace(SecretPlaceCategory.ANCIENT_RELIC_SITE),
        )
        assertNotNull(bundle.storyFragmentId)
        assertTrue(bundle.hasStoryFragment)
    }

    @Test
    fun mysteryZone_grantsTreasureClusterBonus() {
        val bundle = SecretPlaceCategoryRewards.completionBundle(
            samplePlace(SecretPlaceCategory.MYSTERY_ZONE),
        )
        assertTrue(bundle.hasTreasureClusterBonus)
        assertTrue(bundle.treasureClusterBonusSlots >= 1)
    }

    @Test
    fun legendarySite_grantsLegendaryRelic() {
        val bundle = SecretPlaceCategoryRewards.completionBundle(
            samplePlace(SecretPlaceCategory.LEGENDARY_SITE),
        )
        assertNotNull(bundle.legendaryRelicKey)
        assertTrue(bundle.hasLegendaryRelic)
        assertTrue(bundle.hasPanoramaBonus)
        assertTrue(bundle.hasStoryFragment)
    }

    @Test
    fun rewards_scaleWithRarityMultiplier() {
        val common = SecretPlaceCategoryRewards.discoveryBundle(
            samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT, SecretPlaceRarity.COMMON),
        )
        val legendary = SecretPlaceCategoryRewards.discoveryBundle(
            samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT, SecretPlaceRarity.LEGENDARY),
        )
        assertTrue(legendary.credits > common.credits)
        assertTrue(legendary.xp > common.xp)
    }

    @Test
    fun dispatcher_buildsStableCreditGrant() {
        val place = samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT)
        val grant = SecretPlaceCategoryRewardDispatcher.discoveryGrant(place)
        assertNotNull(grant)
        assertEquals("SECRET_PLACE_CATEGORY", grant!!.ruleType)
        assertTrue(grant.eventId.contains(place.secretPlaceId))
    }

    @Test
    fun catalog_definesProfileForEveryCategory() {
        for (category in SecretPlaceCategoryCatalog.displayOrder) {
            val profile = SecretPlaceCategoryRewardCatalog.profileFor(category)
            assertEquals(category, profile.category)
            assertTrue(profile.discoveryCredits >= 0)
            assertTrue(profile.completionCredits >= 0)
            assertTrue(profile.discoveryXp >= 0L)
            assertTrue(profile.completionXp >= 0L)
            assertTrue(profile.effects.contains(SecretPlaceCategoryRewardEffect.CREDITS))
            assertTrue(profile.effects.contains(SecretPlaceCategoryRewardEffect.XP))
        }
    }

    @Test
    fun dispatchDiscovery_natureSanctuary_includesFlowerAndCrystalHookIds() {
        val place = samplePlace(SecretPlaceCategory.NATURE_SANCTUARY)
        val outcome = SecretPlaceCategoryRewardDispatcher.dispatchDiscovery(place)
        assertNotNull(outcome.flowerBonusEventId())
        assertNotNull(outcome.crystalBonusEventId())
        assertNull(outcome.panoramaBonusEventId())
        assertTrue(outcome.xp > 0L)
        assertEquals(SecretPlaceCategoryRewardPhase.DISCOVERY, outcome.phase)
    }

    @Test
    fun dispatchCompletion_scenicViewpoint_includesPanoramaHookId() {
        val place = samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT)
        val outcome = SecretPlaceCategoryRewardDispatcher.dispatchCompletion(place)
        assertNotNull(outcome.panoramaBonusEventId())
        assertTrue(outcome.panoramaBonusEventId()!!.contains("panorama_hunt_scenic_viewpoint"))
        assertTrue(outcome.credits > 0)
    }

    @Test
    fun dispatchDiscovery_historicLandmark_includesStoryAndAchievementHookIds() {
        val place = samplePlace(SecretPlaceCategory.HISTORIC_LANDMARK)
        val outcome = SecretPlaceCategoryRewardDispatcher.dispatchDiscovery(place)
        assertNotNull(outcome.storyFragmentEventId())
        assertNotNull(outcome.achievementEventId())
        assertTrue(outcome.storyFragmentEventId()!!.contains("story_fragment_historic_landmark"))
    }

    @Test
    fun dispatchCompletion_mysteryZone_includesTreasureClusterHookId() {
        val place = samplePlace(SecretPlaceCategory.MYSTERY_ZONE)
        val outcome = SecretPlaceCategoryRewardDispatcher.dispatchCompletion(place)
        assertNotNull(outcome.treasureClusterBonusEventId())
        assertTrue(outcome.bundle.treasureClusterBonusSlots >= 1)
    }

    @Test
    fun dispatchCompletion_legendarySite_includesRelicHookId() {
        val place = samplePlace(SecretPlaceCategory.LEGENDARY_SITE)
        val outcome = SecretPlaceCategoryRewardDispatcher.dispatchCompletion(place)
        assertNotNull(outcome.legendaryRelicEventId())
        assertTrue(outcome.legendaryRelicEventId()!!.contains("legendary_relic_legendary_site"))
    }

    @Test
    fun dispatchOutcome_eventIds_areStablePerPlaceAndPhase() {
        val place = samplePlace(SecretPlaceCategory.MYTHICAL_PLACE)
        val first = SecretPlaceCategoryRewardDispatcher.dispatchCompletion(place)
        val second = SecretPlaceCategoryRewardDispatcher.dispatchCompletion(place)
        assertEquals(first.baseEventId, second.baseEventId)
        assertEquals(first.creditEventId(), second.creditEventId())
        assertEquals(first.xpEventId(), second.xpEventId())
    }

    private fun samplePlace(
        category: SecretPlaceCategory,
        rarity: SecretPlaceRarity = SecretPlaceRarity.COMMON,
    ): SecretPlace = SecretPlace(
        secretPlaceId = "sp:v1:test:${category.id}:seed1",
        category = category,
        latitude = 51.5,
        longitude = -0.12,
        radiusMeters = 80.0,
        rarity = rarity,
        rewardSeed = 1L,
    )
}
