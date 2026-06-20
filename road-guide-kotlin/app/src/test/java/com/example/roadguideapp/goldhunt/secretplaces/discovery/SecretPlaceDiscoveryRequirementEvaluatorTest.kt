package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlaceRarity
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretPlaceDiscoveryRequirementEvaluatorTest {
    @Test
    fun natureSanctuary_requiresSingleVisit() {
        val place = samplePlace(SecretPlaceCategory.NATURE_SANCTUARY)
        val blocked = evaluate(place, context(visitCount = 0))
        val ready = evaluate(place, context(visitCount = 1))
        assertFalse(blocked.isDiscoverable)
        assertTrue(ready.isDiscoverable)
        assertEquals(
            SecretPlaceDiscoveryRequirementType.VISIT_ONCE,
            ready.statusFor(SecretPlaceDiscoveryRequirementType.VISIT_ONCE)!!.requirement.type,
        )
    }

    @Test
    fun scenicViewpoint_requiresReachLocation() {
        val place = samplePlace(SecretPlaceCategory.SCENIC_VIEWPOINT)
        val far = evaluate(
            place,
            context(playerLat = 51.60, playerLng = -0.12),
        )
        val near = evaluate(
            place,
            context(playerLat = 51.5001, playerLng = -0.1201),
        )
        assertFalse(far.isDiscoverable)
        assertTrue(near.isDiscoverable)
    }

    @Test
    fun historicLandmark_requiresNearbyTreasures() {
        val place = samplePlace(SecretPlaceCategory.HISTORIC_LANDMARK)
        val partial = evaluate(place, context(nearbyTreasuresCollected = 1))
        val ready = evaluate(place, context(nearbyTreasuresCollected = 2))
        assertFalse(partial.isDiscoverable)
        assertTrue(ready.isDiscoverable)
    }

    @Test
    fun mysteryZone_requiresClueChain() {
        val place = samplePlace(SecretPlaceCategory.MYSTERY_ZONE)
        val key = SecretPlaceDiscoveryRequirementSchema.ClueChainKeys.forCategory(
            SecretPlaceCategory.MYSTERY_ZONE,
        )
        val partial = evaluate(
            place,
            context(
                clueStepsCompleted = 2,
                activeClueChainKey = key,
            ),
        )
        val ready = evaluate(
            place,
            context(
                clueStepsCompleted = 3,
                activeClueChainKey = key,
            ),
        )
        assertFalse(partial.isDiscoverable)
        assertTrue(ready.isDiscoverable)
    }

    @Test
    fun legendarySite_requiresExplorerLevelAndReachLocation() {
        val place = samplePlace(SecretPlaceCategory.LEGENDARY_SITE)
        val lowLevel = evaluate(
            place,
            context(explorerLevel = 3, playerLat = 51.5, playerLng = -0.12),
        )
        val highLevelFar = evaluate(
            place,
            context(explorerLevel = 8, playerLat = 51.60, playerLng = -0.12),
        )
        val ready = evaluate(
            place,
            context(explorerLevel = 8, playerLat = 51.5001, playerLng = -0.1201),
        )
        assertFalse(lowLevel.isDiscoverable)
        assertFalse(highLevelFar.isDiscoverable)
        assertTrue(ready.isDiscoverable)
        assertNotNull(ready.statusFor(SecretPlaceDiscoveryRequirementType.EXPLORER_LEVEL))
        assertNotNull(ready.statusFor(SecretPlaceDiscoveryRequirementType.REACH_LOCATION))
    }

    @Test
    fun profile_catalog_mapsAllEightCategories() {
        SecretPlaceCategory.entries.forEach { category ->
            val profile = SecretPlaceDiscoveryRequirementCatalog.profileFor(category)
            assertEquals(category, profile.category)
            assertTrue(profile.requirements.isNotEmpty())
        }
    }

    @Test
    fun evaluation_reportsAggregateProgress() {
        val place = samplePlace(SecretPlaceCategory.HISTORIC_LANDMARK)
        val evaluation = evaluate(place, context(nearbyTreasuresCollected = 1))
        assertEquals(0.5f, evaluation.progressFraction, 0.01f)
        assertEquals(0, evaluation.satisfiedCount)
    }

    private fun evaluate(
        place: SecretPlace,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryEvaluation =
        SecretPlaceDiscoveryRequirementEvaluator.evaluate(place, context)

    private fun context(
        playerLat: Double = 51.5,
        playerLng: Double = -0.12,
        visitCount: Int = 0,
        nearbyTreasuresCollected: Int = 0,
        clueStepsCompleted: Int = 0,
        activeClueChainKey: String? = null,
        explorerLevel: Int = 5,
    ): SecretPlaceDiscoveryContext = SecretPlaceDiscoveryContext(
        playerLat = playerLat,
        playerLng = playerLng,
        visitCount = visitCount,
        nearbyTreasuresCollected = nearbyTreasuresCollected,
        clueStepsCompleted = clueStepsCompleted,
        activeClueChainKey = activeClueChainKey,
        explorerLevel = explorerLevel,
    )

    private fun samplePlace(category: SecretPlaceCategory): SecretPlace = SecretPlace(
        secretPlaceId = "sp:v1:test:${category.id}:seed1",
        category = category,
        latitude = 51.5,
        longitude = -0.12,
        radiusMeters = 80.0,
        rarity = SecretPlaceRarity.COMMON,
        rewardSeed = 1L,
    )
}
