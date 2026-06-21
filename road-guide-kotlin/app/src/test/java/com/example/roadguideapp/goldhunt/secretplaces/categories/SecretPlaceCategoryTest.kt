package com.example.roadguideapp.goldhunt.secretplaces.categories

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretPlaceCategoryTest {
    @Test
    fun allOrdered_containsEightCategoriesInDifficultyOrder() {
        assertEquals(8, SecretPlaceCategory.ALL_ORDERED.size)
        assertEquals(SecretPlaceCategory.NATURE_SANCTUARY, SecretPlaceCategory.ALL_ORDERED.first())
        assertEquals(SecretPlaceCategory.LEGENDARY_SITE, SecretPlaceCategory.ALL_ORDERED.last())
        assertTrue(
            SecretPlaceCategory.ALL_ORDERED.zipWithNext().all { (a, b) ->
                a.difficultyLevel < b.difficultyLevel
            },
        )
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(SecretPlaceCategory.MYSTERY_ZONE, SecretPlaceCategory.fromId("mystery_zone"))
        assertNull(SecretPlaceCategory.fromId("UNKNOWN_CATEGORY"))
    }

    @Test
    fun natureSanctuary_hasBaselineTuning() {
        val category = SecretPlaceCategory.NATURE_SANCTUARY
        assertEquals("Nature Sanctuary", category.displayName)
        assertEquals(380, category.rarityWeight)
        assertEquals(1, category.difficultyLevel)
        assertEquals(1.0, category.rewardMultiplier, 0.001)
        assertNull(category.achievementKey)
        assertNull(category.storyFragmentKey)
        assertNull(category.panoramaHuntKey)
        assertNull(category.legendaryRelicKey)
    }

    @Test
    fun legendarySite_reservesFutureHooks() {
        val category = SecretPlaceCategory.LEGENDARY_SITE
        assertNotNull(category.achievementKey)
        assertNotNull(category.storyFragmentKey)
        assertNotNull(category.panoramaHuntKey)
        assertNotNull(category.legendaryRelicKey)
        assertEquals(8, category.difficultyLevel)
        assertEquals(3.0, category.rewardMultiplier, 0.001)
    }

    @Test
    fun catalog_totalRarityWeight_matchesEnumSum() {
        val expected = SecretPlaceCategory.entries.sumOf { it.rarityWeight }
        assertEquals(expected, SecretPlaceCategoryCatalog.totalRarityWeight())
    }

    @Test
    fun schema_buildsAchievementKeys() {
        assertEquals(
            "secret_place_category:nature_sanctuary:first_discovery",
            SecretPlaceCategorySchema.AchievementKeys.firstDiscovery(
                SecretPlaceCategory.NATURE_SANCTUARY,
            ),
        )
        assertEquals(
            "secret_place_category:all_categories_complete",
            SecretPlaceCategorySchema.AchievementKeys.completeAllCategories(),
        )
    }

    @Test
    fun schema_resolvesOptionalHooks() {
        assertNull(
            SecretPlaceCategorySchema.StoryFragmentKeys.forCategory(
                SecretPlaceCategory.SCENIC_VIEWPOINT,
            ),
        )
        assertEquals(
            "story_fragment_ancient_relic_site",
            SecretPlaceCategorySchema.StoryFragmentKeys.forCategory(
                SecretPlaceCategory.ANCIENT_RELIC_SITE,
            ),
        )
        assertEquals(
            "panorama_hunt_explorer_hideout",
            SecretPlaceCategorySchema.PanoramaHuntKeys.forCategory(
                SecretPlaceCategory.EXPLORER_HIDEOUT,
            ),
        )
        assertEquals(
            "legendary_relic_legendary_site",
            SecretPlaceCategorySchema.LegendaryRelicKeys.forCategory(
                SecretPlaceCategory.LEGENDARY_SITE,
            ),
        )
    }
}
