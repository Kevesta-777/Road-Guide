package com.example.roadguideapp.goldhunt.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementSchemaTest {
    @Test
    fun achievementKeys_buildCategoryScopedKeys() {
        assertEquals(
            "achievement:secret_place:first_unlock",
            AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE),
        )
        assertEquals(
            "achievement:seasonal_event:mastery",
            AchievementSchema.AchievementKeys.mastery(AchievementCategory.SEASONAL_EVENT),
        )
    }

    @Test
    fun domainPrefixes_mapToExistingFeatureNamespaces() {
        assertEquals("treasure_rarity", AchievementSchema.DomainPrefixes.forCategory(AchievementCategory.RARITY))
        assertEquals(
            "panorama_hunt_type",
            AchievementSchema.DomainPrefixes.forCategory(AchievementCategory.PANORAMA),
        )
        assertEquals(
            "seasonal_event",
            AchievementSchema.DomainPrefixes.forCategory(AchievementCategory.SEASONAL_EVENT),
        )
    }

    @Test
    fun categoryGates_assignExplorerLevels() {
        assertEquals(1, AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.EXPLORATION))
        assertEquals(20, AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.MASTER_EXPLORER))
        assertTrue(
            AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.PANORAMA) <
                AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.MASTER_EXPLORER),
        )
    }

    @Test
    fun categoryGates_storyAndRelicSupport_matchesCategoryPurpose() {
        assertTrue(AchievementSchema.CategoryGates.supportsStoryFragments(AchievementCategory.STORY))
        assertFalse(AchievementSchema.CategoryGates.supportsStoryFragments(AchievementCategory.TREASURE))
        assertTrue(AchievementSchema.CategoryGates.supportsLegendaryRelics(AchievementCategory.LEGENDARY_RELIC))
        assertFalse(AchievementSchema.CategoryGates.supportsLegendaryRelics(AchievementCategory.RADAR))
    }

    @Test
    fun storyAndRelicKeys_buildReservedHooks() {
        assertEquals(
            "story_fragment_achievement_panorama",
            AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.PANORAMA),
        )
        assertEquals(
            "legendary_relic_achievement_master_explorer",
            AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.MASTER_EXPLORER),
        )
    }

    @Test
    fun titleAndBadgeKeys_reserveFutureCosmeticRewards() {
        assertEquals(
            "title_achievement_master_explorer",
            AchievementSchema.TitleKeys.forCategory(AchievementCategory.MASTER_EXPLORER),
        )
        assertEquals(
            "badge_achievement_panorama",
            AchievementSchema.BadgeKeys.forCategory(AchievementCategory.PANORAMA),
        )
        assertEquals(null, AchievementSchema.TitleKeys.forCategory(AchievementCategory.TREASURE))
        assertEquals(null, AchievementSchema.BadgeKeys.forCategory(AchievementCategory.TREASURE))
    }

    @Test
    fun rewardScaling_appliesDifficultyMultiplier() {
        assertEquals(
            29,
            AchievementSchema.RewardScaling.scaledCredits(AchievementCategory.RARITY, 25),
        )
        assertEquals(
            120L,
            AchievementSchema.RewardScaling.scaledXp(AchievementCategory.MASTER_EXPLORER, 60L),
        )
    }
}
