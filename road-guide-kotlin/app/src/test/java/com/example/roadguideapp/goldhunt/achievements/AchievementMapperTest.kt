package com.example.roadguideapp.goldhunt.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementMapperTest {
    @Test
    fun fromDefinition_buildsIncompleteAchievement() {
        val definition = AchievementCatalog.foundationDefinition(AchievementCategory.PANORAMA)
        val achievement = AchievementMapper.fromDefinition(definition)
        assertEquals(definition.key, achievement.achievementId)
        assertEquals(definition.displayName, achievement.title)
        assertEquals(AchievementCategory.PANORAMA, achievement.category)
        assertEquals(1, achievement.targetValue)
        assertEquals(0, achievement.currentValue)
        assertFalse(achievement.completed)
        assertNull(achievement.completionDate)
        assertTrue(achievement.rewardCredits > 0)
        assertTrue(achievement.rewardXp > 0L)
        assertNotNull(achievement.badgeKey)
    }

    @Test
    fun fromDefinition_completedAchievement_recordsCompletionDate() {
        val definition = AchievementCatalog.foundationDefinition(AchievementCategory.STORY)
        val achievement = AchievementMapper.fromDefinition(
            definition = definition,
            currentValue = 1,
            completed = true,
            completionDate = 5_000L,
        )
        assertTrue(achievement.completed)
        assertEquals(5_000L, achievement.completionDate)
        assertEquals(1, achievement.currentValue)
        assertNotNull(achievement.description)
    }

    @Test
    fun entityRoundTrip_preservesFields() {
        val original = AchievementCatalog.achievementFromDefinition(
            AchievementCatalog.foundationDefinition(AchievementCategory.SEASONAL_EVENT),
            currentValue = 1,
            completed = true,
            completionDate = 9_000L,
        )
        val entity = AchievementMapper.toEntity(original, updatedAtMs = 9_500L)
        val restored = AchievementMapper.toDomain(entity)
        assertNotNull(restored)
        assertEquals(original.achievementId, restored!!.achievementId)
        assertEquals(original.title, restored.title)
        assertEquals(original.description, restored.description)
        assertEquals(original.category, restored.category)
        assertEquals(original.targetValue, restored.targetValue)
        assertEquals(original.currentValue, restored.currentValue)
        assertEquals(original.completed, restored.completed)
        assertEquals(original.completionDate, restored.completionDate)
        assertEquals(original.rewardCredits, restored.rewardCredits)
        assertEquals(original.rewardXp, restored.rewardXp)
        assertEquals(original.titleKey, restored.titleKey)
        assertEquals(original.badgeKey, restored.badgeKey)
    }

    @Test
    fun toDomain_returnsNullForUnknownCategory() {
        val entity = AchievementMapper.toEntity(
            AchievementCatalog.achievementFromDefinition(
                AchievementCatalog.foundationDefinition(AchievementCategory.RADAR),
            ),
        ).copy(category = "UNKNOWN_CATEGORY")
        assertNull(AchievementMapper.toDomain(entity))
    }
}
