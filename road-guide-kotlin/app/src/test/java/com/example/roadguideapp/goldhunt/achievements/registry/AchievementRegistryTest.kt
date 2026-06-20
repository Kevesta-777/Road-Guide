package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRegistryTest {
    @Test
    fun defaultRegistry_registersMoreThanOneHundredAchievements() {
        val snapshot = AchievementRegistry.rebuild()
        assertTrue("expected >= 100 achievements, got ${snapshot.size}", snapshot.size >= 100)
    }

    @Test
    fun defaultRegistry_includesStandardMilestones() {
        val keys = setOf(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.EXPLORATION, 100),
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.EXPLORATION, 500),
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.EXPLORATION, 1_000),
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 100),
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 500),
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 1_000),
        )
        keys.forEach { key ->
            assertNotNull("missing milestone $key", AchievementRegistry.findByKey(key))
        }
    }

    @Test
    fun findByKey_resolvesSeasonalAndFoundationEntries() {
        assertNotNull(
            AchievementRegistry.findByKey(
                AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.TREASURE),
            ),
        )
        assertNotNull(
            AchievementRegistry.findByKey(
                SeasonalEventSchema.AchievementKeys.firstParticipation(
                    com.example.roadguideapp.goldhunt.events.SeasonalEventType.SPRING_BLOSSOM,
                ),
            ),
        )
    }

    @Test
    fun definitionsFor_groupsByCategory() {
        val rarityDefinitions = AchievementRegistry.definitionsFor(AchievementCategory.RARITY)
        assertTrue(rarityDefinitions.size >= 18)
        assertTrue(rarityDefinitions.all { it.category == AchievementCategory.RARITY })
    }

    @Test
    fun builder_rejectsDuplicateKeysAcrossProviders() {
        val duplicateProvider = object : AchievementRegistryProvider {
            override val providerId: String = "duplicate_test"
            override fun definitions(): List<AchievementDefinition> = listOf(
                AchievementRegistry.findByKey(
                    AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.TREASURE),
                )!!,
            )
        }
        try {
            AchievementRegistry.builder(extraProviders = listOf(duplicateProvider)).build()
            throw AssertionError("expected duplicate key failure")
        } catch (error: IllegalStateException) {
            assertTrue(error.message!!.contains("duplicate keys"))
        }
    }

    @Test
    fun builder_supportsCustomFutureProvider() {
        val futureProvider = object : AchievementRegistryProvider {
            override val providerId: String = "future_content"
            override fun definitions(): List<AchievementDefinition> = listOf(
                AchievementRegistryAdapters.simple(
                    key = "achievement:future_content:launch_event",
                    category = AchievementCategory.SEASONAL_EVENT,
                    displayName = "Future launch event",
                ),
            )
        }
        val snapshot = AchievementRegistry.rebuild(extraProviders = listOf(futureProvider))
        assertNotNull(snapshot.findByKey("achievement:future_content:launch_event"))
        assertEquals(1, snapshot.providerContributions["future_content"])
        assertTrue(snapshot.size >= 101)
    }

    @Test
    fun milestoneProvider_supportsCustomThousandPlusScale() {
        val extendedProvider = MilestoneAchievementRegistryProvider(
            category = AchievementCategory.STREAK,
            displayLabel = "Extended streak",
            milestones = listOf(2_500, 5_000),
        )
        val snapshot = AchievementRegistryBuilder()
            .register(extendedProvider)
            .build()
        assertNotNull(
            snapshot.findByKey(
                AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.STREAK, 2_500),
            ),
        )
        assertNotNull(
            snapshot.findByKey(
                AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.STREAK, 5_000),
            ),
        )
    }

    @Test
    fun allAchievements_mapsDefinitionsToPlayerModel() {
        val achievements = AchievementRegistry.allAchievements()
        assertEquals(AchievementRegistry.count(), achievements.size)
        achievements.forEach { achievement ->
            assertNotNull(AchievementRegistry.findByKey(achievement.achievementId))
            assertEquals(achievement.achievementId, achievement.achievementId)
        }
    }

    @Test
    fun achievementByKey_returnsNullForUnknownKey() {
        assertNull(AchievementRegistry.achievementByKey("achievement:missing:key"))
    }
}
