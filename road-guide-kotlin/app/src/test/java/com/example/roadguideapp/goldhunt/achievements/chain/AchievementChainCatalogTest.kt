package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementChainCatalogTest {
    @Test
    fun gates_referenceRegisteredAchievements() {
        AchievementChainCatalog.gates.forEach { gate ->
            assertNotNull(
                "Missing gate target ${gate.achievementKey}",
                AchievementRegistry.findByKey(gate.achievementKey),
            )
            gate.dependencies.forEach { dependency ->
                if (dependency is AchievementChainDependency.AchievementCompleted) {
                    assertNotNull(
                        "Missing dependency ${dependency.achievementKey}",
                        AchievementRegistry.findByKey(dependency.achievementKey),
                    )
                }
            }
        }
    }

    @Test
    fun rewards_referenceRegisteredSourceAchievements() {
        AchievementChainCatalog.rewards.forEach { reward ->
            assertNotNull(
                "Missing reward source ${reward.sourceAchievementKey}",
                AchievementRegistry.findByKey(reward.sourceAchievementKey),
            )
            assertFalse(reward.unlocks.isEmpty())
        }
    }

    @Test
    fun treasureMilestoneChain_links100To500To1000() {
        val treasure100 = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 100)
        val treasure500 = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 500)
        val treasure1000 = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 1_000)

        val gate500 = AchievementChainCatalog.gateFor(treasure500)
        val gate1000 = AchievementChainCatalog.gateFor(treasure1000)

        assertNotNull(gate500)
        assertNotNull(gate1000)
        assertTrue(
            gate500!!.dependencies.any {
                it is AchievementChainDependency.AchievementCompleted &&
                    it.achievementKey == treasure100
            },
        )
        assertTrue(
            gate1000!!.dependencies.any {
                it is AchievementChainDependency.AchievementCompleted &&
                    it.achievementKey == treasure500
            },
        )
    }
}
