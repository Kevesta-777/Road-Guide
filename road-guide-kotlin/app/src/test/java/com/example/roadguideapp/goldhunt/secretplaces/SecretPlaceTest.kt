package com.example.roadguideapp.goldhunt.secretplaces

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretPlaceTest {
    @Test
    fun resolvedHooks_fallBackToCategory() {
        val place = samplePlace(
            category = SecretPlaceCategory.ANCIENT_RELIC_SITE,
        )
        assertEquals("story_fragment_ancient_relic_site", place.resolvedStoryFragmentId)
        assertEquals("secret_place_ancient_relic_site", place.resolvedAchievementKey)
        assertEquals(5, place.resolvedMinimumExplorerLevel)
    }

    @Test
    fun instanceHooks_overrideCategoryDefaults() {
        val place = samplePlace(
            category = SecretPlaceCategory.NATURE_SANCTUARY,
            minimumExplorerLevel = 4,
            storyFragmentId = "story_custom",
            legendaryRelicKey = "relic_custom",
            achievementKey = "achievement_custom",
            linkedClusterIds = listOf("tc:test:1", "tc:test:2"),
        )
        assertEquals("story_custom", place.resolvedStoryFragmentId)
        assertEquals("relic_custom", place.resolvedLegendaryRelicKey)
        assertEquals("achievement_custom", place.resolvedAchievementKey)
        assertEquals(4, place.resolvedMinimumExplorerLevel)
        assertTrue(place.hasClusterLinks)
        assertTrue(place.hasStoryFragmentHook)
        assertTrue(place.hasLegendaryRelicHook)
    }

    @Test
    fun effectiveRewardMultiplier_combinesCategoryAndRarity() {
        val place = samplePlace(
            category = SecretPlaceCategory.MYTHICAL_PLACE,
            rarity = SecretPlaceRarity.LEGENDARY,
        )
        assertEquals(2.2 * 3.0, place.effectiveRewardMultiplier, 0.001)
    }

    @Test
    fun explorerLevelGate_blocksLowLevelPlayers() {
        val place = samplePlace(category = SecretPlaceCategory.EXPLORER_HIDEOUT)
        assertFalse(place.isAccessibleAtLevel(3))
        assertTrue(place.isAccessibleAtLevel(6))
        assertEquals(3, SecretPlaceExplorerLevelGate.levelsUntilAccessible(place, 3))
    }

    @Test
    fun withDiscovered_clearsCompletionWhenUndiscovered() {
        val place = samplePlace(
            discovered = true,
            completed = true,
            discoveryDate = 100L,
            completionDate = 200L,
        )
        val reset = place.withDiscovered(discovered = false, discoveryDate = null)
        assertFalse(reset.discovered)
        assertFalse(reset.completed)
        assertEquals(null, reset.discoveryDate)
        assertEquals(null, reset.completionDate)
    }

    @Test
    fun withCompleted_marksDiscoveredAndSetsDates() {
        val place = samplePlace(discovered = false, completed = false)
        val done = place.withCompleted(completed = true, completionDate = 500L)
        assertTrue(done.discovered)
        assertTrue(done.completed)
        assertEquals(500L, done.discoveryDate)
        assertEquals(500L, done.completionDate)
    }

    @Test
    fun withLinkedClusterId_appendsUniqueIds() {
        val place = samplePlace()
        val linked = place.withLinkedClusterId("tc:a").withLinkedClusterId("tc:b")
        assertEquals(listOf("tc:a", "tc:b"), linked.linkedClusterIds)
        assertEquals(linked, linked.withLinkedClusterId("tc:a"))
    }

    @Test
    fun secretPlaceId_forCategoryAnchor_isStable() {
        val id = SecretPlaceId.forCategoryAnchor(
            playRegionId = "london",
            category = SecretPlaceCategory.MYSTERY_ZONE,
            rewardSeed = 77L,
        )
        assertEquals("sp:v1:london:MYSTERY_ZONE:seed77", id)
        val parsed = SecretPlaceId.parse(id)
        assertNotNull(parsed)
        assertEquals("london", parsed!!.playRegionId)
        assertEquals("MYSTERY_ZONE", parsed.categoryId)
        assertEquals(77L, parsed.rewardSeed)
    }

    @Test
    fun fromIdParts_resolvesCategory() {
        val place = SecretPlace.fromIdParts(
            secretPlaceId = "sp:v1:london:LEGENDARY_SITE:seed9",
            categoryId = "legendary_site",
            latitude = 51.5,
            longitude = -0.12,
            radiusMeters = 80.0,
            rarity = SecretPlaceRarity.LEGENDARY,
            rewardSeed = 9L,
        )
        assertNotNull(place)
        assertEquals(SecretPlaceCategory.LEGENDARY_SITE, place!!.category)
    }

    private fun samplePlace(
        category: SecretPlaceCategory = SecretPlaceCategory.SCENIC_VIEWPOINT,
        rarity: SecretPlaceRarity = SecretPlaceRarity.COMMON,
        discovered: Boolean = false,
        completed: Boolean = false,
        discoveryDate: Long? = null,
        completionDate: Long? = null,
        minimumExplorerLevel: Int? = null,
        linkedClusterIds: List<String> = emptyList(),
        storyFragmentId: String? = null,
        legendaryRelicKey: String? = null,
        achievementKey: String? = null,
    ): SecretPlace = SecretPlace(
        secretPlaceId = "sp:v1:test:${category.id}:seed1",
        category = category,
        latitude = 51.5074,
        longitude = -0.1278,
        radiusMeters = 75.0,
        rarity = rarity,
        discovered = discovered,
        completed = completed,
        discoveryDate = discoveryDate,
        completionDate = completionDate,
        rewardSeed = 1L,
        minimumExplorerLevel = minimumExplorerLevel,
        linkedClusterIds = linkedClusterIds,
        storyFragmentId = storyFragmentId,
        legendaryRelicKey = legendaryRelicKey,
        achievementKey = achievementKey,
    )
}
