package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatisticsSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementProgressEvaluatorTest {
    @Test
    fun counterValue_usesMaxOfLegacyAndDerivedCounters() {
        val definition = AchievementRegistry.findByKey(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 100),
        )!!
        val counters = AchievementProgressCounters(
            treasuresCollected = 80,
            legacyCompletionCounts = mapOf(definition.key to 95),
        )
        assertEquals(95, AchievementProgressEvaluator.counterValue(definition, counters))
    }

    @Test
    fun counterValue_mapsRarityMilestonesFromProfileCounts() {
        val definition = AchievementRegistry.allDefinitions().first {
            it.key == TreasureRarityStatisticsSchema.AchievementKeys.countMilestone(
                TreasureRarity.RARE.id.lowercase(),
                500,
            )
        }
        val counters = AchievementProgressCounters(
            rarityCounts = mapOf(TreasureRarity.RARE to 512),
        )
        assertEquals(512, AchievementProgressEvaluator.counterValue(definition, counters))
    }

    @Test
    fun counterValue_mapsMasterExplorerLevelTargets() {
        val definition = AchievementRegistry.allDefinitions().first {
            it.key == AchievementSchema.AchievementKeys.forCategory(
                AchievementCategory.MASTER_EXPLORER,
                "level_20",
            )
        }
        val counters = AchievementProgressCounters(explorerLevel = 18)
        assertEquals(18, AchievementProgressEvaluator.counterValue(definition, counters))
        val completedCounters = counters.copy(explorerLevel = 22)
        assertEquals(20, AchievementProgressEvaluator.counterValue(definition, completedCounters))
    }

    @Test
    fun categoriesForEvent_coverGameplaySources() {
        assertTrue(
            AchievementProgressEvaluator.categoriesForEvent(
                AchievementProgressEvent.TreasureCollected(),
            ).contains(AchievementCategory.RARITY),
        )
        assertTrue(
            AchievementProgressEvaluator.categoriesForEvent(
                AchievementProgressEvent.ProfileSynced(),
            ).contains(AchievementCategory.MASTER_EXPLORER),
        )
    }
}
