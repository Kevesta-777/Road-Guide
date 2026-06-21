package com.example.roadguideapp.goldhunt.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogTest {
    @Test
    fun foundationDefinitions_coverAllCategories() {
        val definitions = AchievementCatalog.foundationDefinitions()
        assertEquals(AchievementCategory.ALL_ORDERED.size, definitions.size)
        assertEquals(
            AchievementCategory.ALL_ORDERED.toSet(),
            definitions.map { it.category }.toSet(),
        )
    }

    @Test
    fun findByKey_resolvesFoundationDefinition() {
        val key = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.TREASURE_CLUSTER)
        val definition = AchievementCatalog.findByKey(key)
        assertNotNull(definition)
        assertEquals(AchievementCategory.TREASURE_CLUSTER, definition!!.category)
        assertEquals("Treasure Cluster — first unlock", definition.displayName)
    }

    @Test
    fun findByKey_returnsNullForUnknownKey() {
        assertNull(AchievementCatalog.findByKey("achievement:unknown:first_unlock"))
    }

    @Test
    fun foundationDefinition_assignsHooksForEligibleCategories() {
        val storyDefinition = AchievementCatalog.foundationDefinition(AchievementCategory.STORY)
        assertNotNull(storyDefinition.storyFragmentKey)
        assertNull(storyDefinition.legendaryRelicKey)

        val relicDefinition = AchievementCatalog.foundationDefinition(AchievementCategory.LEGENDARY_RELIC)
        assertNotNull(relicDefinition.storyFragmentKey)
        assertNotNull(relicDefinition.legendaryRelicKey)
    }

    @Test
    fun foundationDefinition_usesCategoryGatesForExplorerLevel() {
        val definition = AchievementCatalog.foundationDefinition(AchievementCategory.MASTER_EXPLORER)
        assertEquals(20, definition.minExplorerLevel)
        assertEquals("achievement_master_explorer", definition.iconKey)
    }

    @Test
    fun foundationAchievements_mapDefinitionsToPlayerModel() {
        val achievements = AchievementCatalog.foundationAchievements()
        assertEquals(AchievementCategory.ALL_ORDERED.size, achievements.size)
        achievements.forEach { achievement ->
            assertEquals(1, achievement.targetValue)
            assertEquals(0, achievement.currentValue)
            assertFalse(achievement.completed)
            assertNull(achievement.completionDate)
        }
    }

    @Test
    fun scaledCompletionRewards_applyCategoryDifficulty() {
        val baseline = AchievementCatalog.scaledCompletionCredits(AchievementCategory.TREASURE)
        val scaled = AchievementCatalog.scaledCompletionCredits(AchievementCategory.SEASONAL_EVENT)
        assertEquals(25, baseline)
        assertTrue(scaled > baseline)
    }
}
