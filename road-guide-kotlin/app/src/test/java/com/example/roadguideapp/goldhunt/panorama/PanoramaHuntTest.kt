package com.example.roadguideapp.goldhunt.panorama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntTest {
    @Test
    fun resolvedHooks_fallBackToHuntType() {
        val hunt = sampleHunt(huntType = PanoramaHuntType.RELIC_HUNT)
        assertEquals("story_fragment_relic_hunt", hunt.resolvedStoryFragmentId)
        assertEquals("panorama_hunt_relic_hunt", hunt.resolvedAchievementKey)
        assertEquals("legendary_relic_panorama_hunt", hunt.resolvedLegendaryRelicKey)
        assertEquals(5, hunt.resolvedMinimumExplorerLevel)
        assertEquals(5, hunt.difficulty)
    }

    @Test
    fun instanceHooks_overrideHuntTypeDefaults() {
        val hunt = sampleHunt(
            huntType = PanoramaHuntType.HIDDEN_SYMBOL,
            minimumExplorerLevel = 3,
            storyFragmentId = "story_custom",
            legendaryRelicKey = "relic_custom",
            achievementKey = "achievement_custom",
            difficulty = 2,
        )
        assertEquals("story_custom", hunt.resolvedStoryFragmentId)
        assertEquals("relic_custom", hunt.resolvedLegendaryRelicKey)
        assertEquals("achievement_custom", hunt.resolvedAchievementKey)
        assertEquals(3, hunt.resolvedMinimumExplorerLevel)
        assertEquals(2, hunt.difficulty)
        assertTrue(hunt.hasStoryFragmentHook)
        assertTrue(hunt.hasLegendaryRelicHook)
        assertTrue(hunt.hasAchievementHook)
    }

    @Test
    fun explorerLevelGate_blocksLowLevelPlayers() {
        val hunt = sampleHunt(huntType = PanoramaHuntType.SECRET_CODE)
        assertFalse(hunt.isAccessibleAtLevel(2))
        assertTrue(hunt.isAccessibleAtLevel(4))
        assertEquals(2, PanoramaHuntExplorerLevelGate.levelsUntilAccessible(hunt, 2))
    }

    @Test
    fun withCompleted_setsCompletionState() {
        val hunt = sampleHunt(completed = false)
        val done = hunt.withCompleted(completed = true, completionDate = 900L)
        assertTrue(done.completed)
        assertEquals(900L, done.completionDate)
        val reset = done.withCompleted(completed = false)
        assertFalse(reset.completed)
        assertNull(reset.completionDate)
    }

    @Test
    fun forSecretPlace_buildsStableHuntId() {
        val hunt = PanoramaHunt.forSecretPlace(
            secretPlaceId = "sp:v1:london:SCENIC_VIEWPOINT:seed7",
            huntType = PanoramaHuntType.OBJECT_HUNT,
            panoramaId = "pano-abc123",
            rewardSeed = 42L,
        )
        assertEquals(
            "ph:v1:sp:v1:london:SCENIC_VIEWPOINT:seed7:OBJECT_HUNT:pano-abc123:seed42",
            hunt.huntId,
        )
        assertEquals("sp:v1:london:SCENIC_VIEWPOINT:seed7", hunt.secretPlaceId)
        assertEquals("pano-abc123", hunt.panoramaId)
        assertEquals(42L, hunt.rewardSeed)
        assertEquals(2, hunt.difficulty)
    }

    @Test
    fun huntId_parse_roundTrips() {
        val huntId = PanoramaHuntId.forSecretPlace(
            secretPlaceId = "sp:v1:test:RELIC_HUNT:seed9",
            huntType = PanoramaHuntType.RELIC_HUNT,
            panoramaId = "pano-xyz",
            rewardSeed = 9L,
        )
        val parsed = PanoramaHuntId.parse(huntId)
        assertNotNull(parsed)
        assertEquals("sp:v1:test:RELIC_HUNT:seed9", parsed!!.secretPlaceId)
        assertEquals("RELIC_HUNT", parsed.huntTypeId)
        assertEquals("pano-xyz", parsed.panoramaId)
        assertEquals(9L, parsed.rewardSeed)
    }

    @Test
    fun fromIdParts_resolvesHuntType() {
        val hunt = PanoramaHunt.fromIdParts(
            huntId = "ph:v1:sp:test:HIDDEN_SYMBOL:pano1:seed1",
            huntTypeId = "HIDDEN_SYMBOL",
            secretPlaceId = "sp:test",
            panoramaId = "pano1",
            rewardSeed = 1L,
        )
        assertNotNull(hunt)
        assertEquals(PanoramaHuntType.HIDDEN_SYMBOL, hunt!!.huntType)
    }

    @Test
    fun instanceKey_alignsWithSchema() {
        val hunt = sampleHunt(huntType = PanoramaHuntType.HIDDEN_SYMBOL)
        assertEquals(
            "panorama_hunt:place:sp:v1:test:HIDDEN_SYMBOL:seed1:hidden_symbol",
            hunt.instanceKey,
        )
    }

    private fun sampleHunt(
        huntType: PanoramaHuntType = PanoramaHuntType.PANORAMA_PUZZLE,
        completed: Boolean = false,
        completionDate: Long? = if (completed) 500L else null,
        minimumExplorerLevel: Int? = null,
        storyFragmentId: String? = null,
        legendaryRelicKey: String? = null,
        achievementKey: String? = null,
        difficulty: Int? = null,
    ): PanoramaHunt = PanoramaHunt(
        huntId = "ph:v1:sp:v1:test:${huntType.id}:seed1:pano1:seed1",
        huntType = huntType,
        secretPlaceId = "sp:v1:test:${huntType.id}:seed1",
        panoramaId = "pano1",
        difficulty = difficulty ?: PanoramaHuntSchema.defaultDifficulty(huntType),
        rewardSeed = 1L,
        completed = completed,
        completionDate = completionDate,
        minimumExplorerLevel = minimumExplorerLevel,
        storyFragmentId = storyFragmentId,
        legendaryRelicKey = legendaryRelicKey,
        achievementKey = achievementKey,
    )
}
