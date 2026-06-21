package com.example.roadguideapp.goldhunt.achievements.titles

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTitleMapperTest {
    private val definition = AchievementTitleDefinition(
        titleKey = AchievementSchema.TitleKeys.forCategory(AchievementCategory.STREAK)!!,
        achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.STREAK),
        displayTitle = "Streak — first unlock",
        rarity = AchievementTitleRarity.RARE,
    )

    @Test
    fun toDomain_marksActiveTitle() {
        val entity = AchievementTitleMapper.toEntity(
            definition = definition,
            unlockDateMs = 2_000L,
            timestampMs = 2_000L,
        )
        val entry = AchievementTitleMapper.toDomain(
            definition = definition,
            entity = entity,
            activeTitleKey = definition.titleKey,
        )
        assertTrue(entry.isActive)
        assertTrue(entry.isUnlocked)
    }

    @Test
    fun toDomain_lockedWhenUnlockDateMissing() {
        val entity = AchievementTitleMapper.toEntity(
            definition = definition,
            unlockDateMs = null,
            timestampMs = 1_000L,
        )
        val entry = AchievementTitleMapper.toDomain(definition, entity, null)
        assertFalse(entry.isUnlocked)
        assertFalse(entry.isActive)
    }

    @Test
    fun buildProgress_countsSingleActiveTitle() {
        val unlocked = AchievementTitleEntry(
            titleKey = definition.titleKey,
            achievementId = definition.achievementId,
            displayTitle = definition.displayTitle,
            rarity = definition.rarity,
            unlockDateMs = 1_000L,
            isActive = true,
        )
        val locked = unlocked.copy(
            titleKey = "title_achievement_legendary_relic",
            isActive = false,
            unlockDateMs = null,
        )
        val progress = AchievementTitleMapper.buildProgress(listOf(unlocked, locked))
        assertEquals(1, progress.activeCount)
        assertEquals(1, progress.unlockedCount)
    }
}
