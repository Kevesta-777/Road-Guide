package com.example.roadguideapp.goldhunt.relics

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicSchemaTest {
    @Test
    fun relicKeys_forCategory_usesStableNamespace() {
        assertEquals(
            "legendary_relic_royal",
            RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
        )
        assertEquals(
            "legendary_relic_royal",
            RelicSchema.RelicKeys.forCategoryId("royal"),
        )
    }

    @Test
    fun achievementKeys_buildCategoryProgressionFamily() {
        val category = RelicCategory.MYTHICAL
        assertEquals(
            "relic_category:mythical:first_discovery",
            RelicSchema.AchievementKeys.firstDiscovery(category),
        )
        assertEquals(
            "relic_category:mythical:collector",
            RelicSchema.AchievementKeys.collector(category),
        )
        assertEquals(
            "relic_category:mythical:mastery",
            RelicSchema.AchievementKeys.mastery(category),
        )
    }

    @Test
    fun storyFragmentKeys_forCategory_usesStableNamespace() {
        assertEquals(
            "story_fragment_relic_story",
            RelicSchema.StoryFragmentKeys.forCategory(RelicCategory.STORY),
        )
    }

    @Test
    fun storyFragmentKeys_forRelic_buildsChapterLoreAndCompletionNamespaces() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER)
        assertEquals("story_fragment_relic:$relicId", RelicSchema.StoryFragmentKeys.forRelic(relicId))
        assertEquals(
            "story_fragment_relic:$relicId:completion",
            RelicSchema.StoryFragmentKeys.completionForRelic(relicId),
        )
        assertEquals(
            "story_fragment_relic:$relicId:chapter_1",
            RelicSchema.StoryFragmentKeys.chapterForRelic(relicId, 1),
        )
        assertEquals(
            "story_fragment_relic:$relicId:lore_0",
            RelicSchema.StoryFragmentKeys.loreForRelic(relicId, 0),
        )
    }

    @Test
    fun secretPlaceKeys_linksMappedCategories() {
        assertEquals(
            SecretPlaceCategory.ANCIENT_RELIC_SITE,
            RelicSchema.SecretPlaceKeys.linkedSecretPlaceCategory(RelicCategory.ANCIENT_CIVILIZATION),
        )
        assertEquals(
            "legendary_relic_secret_place_legendary_site",
            RelicSchema.SecretPlaceKeys.forCategory(RelicCategory.ROYAL),
        )
        assertNull(RelicSchema.SecretPlaceKeys.linkedSecretPlaceCategory(RelicCategory.SEASONAL_EVENT))
    }

    @Test
    fun categoryGates_respectsMinimumExplorerLevel() {
        assertFalse(
            RelicSchema.CategoryGates.isUnlocked(
                category = RelicCategory.ROYAL,
                explorerLevel = 20,
            ),
        )
        assertTrue(
            RelicSchema.CategoryGates.isUnlocked(
                category = RelicCategory.ROYAL,
                explorerLevel = 25,
            ),
        )
    }

    @Test
    fun rewardScaling_scalesByRarityMultiplier() {
        assertEquals(
            250,
            RelicSchema.RewardScaling.scaledCredits(RelicCategory.ROYAL, baseCredits = 100),
        )
        assertEquals(
            300L,
            RelicSchema.RewardScaling.scaledXp(RelicCategory.HIDDEN, baseXp = 100L),
        )
    }

    @Test
    fun pieceKeys_forPiece_usesStableNamespace() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL)
        assertEquals(
            "relic_piece:$relicId:3",
            RelicSchema.PieceKeys.forPiece(relicId, pieceNumber = 3),
        )
    }

    @Test
    fun panoramaHuntKeys_forCategory_rotatesHuntFamilies() {
        val key = RelicSchema.PanoramaHuntKeys.forCategory(RelicCategory.MYTHICAL, pieceNumber = 2)
        assertTrue(key.startsWith("panorama_hunt_relic_piece:mythical:"))
    }

    @Test
    fun futureRewardKeys_useStableNamespaces() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL)
        assertEquals("badge_relic:$relicId", RelicSchema.BadgeKeys.forRelic(relicId))
        assertEquals("title_relic:$relicId", RelicSchema.TitleKeys.forRelic(relicId))
        assertEquals("relic_power:$relicId", RelicSchema.PowerKeys.forRelic(relicId))
    }

    @Test
    fun pieceSourceKeys_buildStableNamespaces() {
        val category = SecretPlaceCategory.LEGENDARY_SITE
        assertEquals(
            "relic_piece_source:secret_place:legendary_site",
            RelicSchema.PieceSourceKeys.SecretPlace.forCategory(category),
        )
        assertEquals(
            "relic_piece_source:treasure_cluster:ancient_vault",
            RelicSchema.PieceSourceKeys.TreasureCluster.forType(
                com.example.roadguideapp.goldhunt.clusters.ClusterType.ANCIENT_VAULT,
            ),
        )
        assertEquals(
            "relic_piece_source:radar:treasure_radar",
            RelicSchema.PieceSourceKeys.RadarDiscovery.forType(
                com.example.roadguideapp.goldhunt.radar.RadarType.TREASURE_RADAR,
            ),
        )
    }

    @Test
    fun categoryGates_declaresFutureIntegrationSupport() {
        assertTrue(RelicSchema.CategoryGates.supportsAchievements(RelicCategory.EXPLORER))
        assertFalse(RelicSchema.CategoryGates.supportsAchievements(RelicCategory.HIDDEN))
        assertTrue(RelicSchema.CategoryGates.supportsStoryFragments(RelicCategory.STORY))
        assertFalse(RelicSchema.CategoryGates.supportsStoryFragments(RelicCategory.WARRIOR))
        assertTrue(RelicSchema.CategoryGates.supportsSecretPlaces(RelicCategory.MYTHICAL))
        assertFalse(RelicSchema.CategoryGates.supportsSecretPlaces(RelicCategory.SEASONAL_EVENT))
        assertNotNull(RelicSchema.SecretPlaceKeys.linkedSecretPlaceCategoryId(RelicCategory.EXPLORER))
    }
}
