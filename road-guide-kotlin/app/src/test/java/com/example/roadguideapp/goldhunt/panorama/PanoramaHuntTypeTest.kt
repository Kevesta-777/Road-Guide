package com.example.roadguideapp.goldhunt.panorama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntTypeTest {
    @Test
    fun allOrdered_containsFiveTypesInDifficultyOrder() {
        assertEquals(5, PanoramaHuntType.ALL_ORDERED.size)
        assertEquals(PanoramaHuntType.HIDDEN_SYMBOL, PanoramaHuntType.ALL_ORDERED.first())
        assertEquals(PanoramaHuntType.RELIC_HUNT, PanoramaHuntType.ALL_ORDERED.last())
        assertTrue(
            PanoramaHuntType.ALL_ORDERED.zipWithNext().all { (a, b) ->
                a.difficulty < b.difficulty
            },
        )
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(PanoramaHuntType.SECRET_CODE, PanoramaHuntType.fromId("secret_code"))
        assertNull(PanoramaHuntType.fromId("UNKNOWN_HUNT"))
    }

    @Test
    fun hiddenSymbol_hasBaselineTuning() {
        val type = PanoramaHuntType.HIDDEN_SYMBOL
        assertEquals("Hidden Symbol", type.displayName)
        assertEquals(1, type.difficulty)
        assertEquals(1.0, type.rewardMultiplier, 0.001)
        assertNotNull(type.achievementKey)
        assertNull(type.storyFragmentKey)
        assertNull(type.legendaryRelicKey)
    }

    @Test
    fun relicHunt_reservesFutureHooks() {
        val type = PanoramaHuntType.RELIC_HUNT
        assertNotNull(type.achievementKey)
        assertNotNull(type.storyFragmentKey)
        assertNotNull(type.legendaryRelicKey)
        assertEquals(5, type.difficulty)
        assertEquals(2.0, type.rewardMultiplier, 0.001)
    }

    @Test
    fun catalog_mapsAllTypes() {
        PanoramaHuntTypeCatalog.displayOrder.forEach { type ->
            assertEquals(type, PanoramaHuntTypeCatalog.fromId(type.id))
            assertEquals(type.difficulty, PanoramaHuntTypeCatalog.difficultyOf(type))
            assertEquals(type.rewardMultiplier, PanoramaHuntTypeCatalog.rewardMultiplierOf(type), 0.001)
        }
    }

    @Test
    fun schema_buildsAchievementKeys() {
        assertEquals(
            "panorama_hunt_type:hidden_symbol:first_completion",
            PanoramaHuntTypeSchema.AchievementKeys.firstCompletion(PanoramaHuntType.HIDDEN_SYMBOL),
        )
        assertEquals(
            "panorama_hunt_type:all_types_complete",
            PanoramaHuntTypeSchema.AchievementKeys.completeAllTypes(),
        )
    }

    @Test
    fun schema_resolvesOptionalHooks() {
        assertNull(
            PanoramaHuntTypeSchema.StoryFragmentKeys.forType(PanoramaHuntType.OBJECT_HUNT),
        )
        assertEquals(
            "story_fragment_secret_code",
            PanoramaHuntTypeSchema.StoryFragmentKeys.forType(PanoramaHuntType.SECRET_CODE),
        )
        assertEquals(
            "legendary_relic_panorama_hunt",
            PanoramaHuntTypeSchema.LegendaryRelicKeys.forType(PanoramaHuntType.RELIC_HUNT),
        )
    }

    @Test
    fun schema_buildsHuntInstanceKeys() {
        assertEquals(
            "panorama_hunt:place:sp:v1:test:seed1:hidden_symbol",
            PanoramaHuntTypeSchema.HuntInstanceKeys.forSecretPlace(
                "sp:v1:test:seed1",
                PanoramaHuntType.HIDDEN_SYMBOL,
            ),
        )
        assertEquals(
            "panorama_hunt:category:SCENIC_VIEWPOINT:object_hunt",
            PanoramaHuntTypeSchema.HuntInstanceKeys.forCategoryAnchor(
                "SCENIC_VIEWPOINT",
                PanoramaHuntType.OBJECT_HUNT,
            ),
        )
    }
}
