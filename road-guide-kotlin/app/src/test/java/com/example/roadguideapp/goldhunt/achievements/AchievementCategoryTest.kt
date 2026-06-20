package com.example.roadguideapp.goldhunt.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCategoryTest {
    @Test
    fun allOrdered_containsTwelveCategoriesInProgressionOrder() {
        assertEquals(12, AchievementCategory.ALL_ORDERED.size)
        assertEquals(AchievementCategory.EXPLORATION, AchievementCategory.ALL_ORDERED.first())
        assertEquals(AchievementCategory.MASTER_EXPLORER, AchievementCategory.ALL_ORDERED.last())
    }

    @Test
    fun difficultyMultipliers_anchorBaselineAndCapstone() {
        assertEquals(1.0, AchievementCategory.EXPLORATION.difficultyMultiplier, 0.001)
        assertEquals(1.0, AchievementCategory.TREASURE.difficultyMultiplier, 0.001)
        assertEquals(2.0, AchievementCategory.MASTER_EXPLORER.difficultyMultiplier, 0.001)
        assertTrue(
            AchievementCategory.ALL_ORDERED.all {
                it.difficultyMultiplier <= AchievementCategory.MASTER_EXPLORER.difficultyMultiplier
            },
        )
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(AchievementCategory.SEASONAL_EVENT, AchievementCategory.fromId("seasonal_event"))
        assertNull(AchievementCategory.fromId("UNKNOWN_CATEGORY"))
    }

    @Test
    fun exploration_hasBaselineMetadata() {
        val category = AchievementCategory.EXPLORATION
        assertEquals("Exploration", category.displayName)
        assertEquals("achievement_exploration", category.iconKey)
        assertEquals(1.0, category.difficultyMultiplier, 0.001)
    }

    @Test
    fun legendaryRelic_hasElevatedDifficulty() {
        val category = AchievementCategory.LEGENDARY_RELIC
        assertEquals("Legendary Relic", category.displayName)
        assertEquals("achievement_legendary_relic", category.iconKey)
        assertTrue(category.difficultyMultiplier > AchievementCategory.TREASURE.difficultyMultiplier)
    }
}
