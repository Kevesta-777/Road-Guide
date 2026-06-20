package com.example.roadguideapp.goldhunt.achievements.badges

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementBadgeMapperTest {
    private val definition = AchievementBadgeDefinition(
        badgeKey = AchievementSchema.BadgeKeys.forCategory(AchievementCategory.PANORAMA)!!,
        achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.PANORAMA),
        title = "Panorama — first unlock",
        iconKey = "achievement_panorama",
        rarity = AchievementBadgeRarity.RARE,
    )

    @Test
    fun toDomain_lockedWhenUnlockDateMissing() {
        val entity = AchievementBadgeMapper.toEntity(
            definition = definition,
            unlockDateMs = null,
            timestampMs = 1_000L,
        )
        val entry = AchievementBadgeMapper.toDomain(definition, entity)
        assertFalse(entry.isUnlocked)
        assertTrue(entry.isLocked)
    }

    @Test
    fun toDomain_unlockedWhenUnlockDatePresent() {
        val entity = AchievementBadgeMapper.toEntity(
            definition = definition,
            unlockDateMs = 5_000L,
            timestampMs = 5_000L,
        )
        val entry = AchievementBadgeMapper.toDomain(definition, entity)
        assertTrue(entry.isUnlocked)
        assertEquals(5_000L, entry.unlockDateMs)
    }

    @Test
    fun buildProgress_countsUnlockedAndLocked() {
        val unlocked = AchievementBadgeEntry(
            badgeKey = definition.badgeKey,
            achievementId = definition.achievementId,
            title = definition.title,
            iconKey = definition.iconKey,
            rarity = definition.rarity,
            unlockDateMs = 1_000L,
        )
        val locked = unlocked.copy(
            badgeKey = "badge_achievement_master_explorer",
            unlockDateMs = null,
        )
        val progress = AchievementBadgeMapper.buildProgress(listOf(unlocked, locked))
        assertEquals(2, progress.totalCount)
        assertEquals(1, progress.unlockedCount)
        assertEquals(1, progress.lockedCount)
        assertEquals(0.5f, progress.completionFraction, 0.0001f)
    }
}
