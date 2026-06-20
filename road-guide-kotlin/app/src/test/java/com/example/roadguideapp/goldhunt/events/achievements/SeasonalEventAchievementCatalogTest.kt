package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEventAchievementCatalogTest {
    @Test
    fun eventAchievements_includeParticipationCompletionAndMastery() {
        val achievements = SeasonalEventAchievementCatalog.eventAchievements(
            SeasonalEventType.HALLOWEEN_MYSTERY,
        )

        assertEquals(3, achievements.size)
        assertEquals(
            SeasonalEventSchema.AchievementKeys.firstParticipation(SeasonalEventType.HALLOWEEN_MYSTERY),
            achievements.first { it.tier == SeasonalEventAchievementTier.FIRST_PARTICIPATION }.key,
        )
        assertEquals(
            SeasonalEventSchema.AchievementKeys.completion(SeasonalEventType.HALLOWEEN_MYSTERY),
            achievements.first { it.tier == SeasonalEventAchievementTier.COMPLETION }.key,
        )
        assertEquals(
            SeasonalEventSchema.AchievementKeys.mastery(SeasonalEventType.HALLOWEEN_MYSTERY),
            achievements.first { it.tier == SeasonalEventAchievementTier.MASTERY }.key,
        )
    }

    @Test
    fun categoryCollector_targetMatchesMemberCount() {
        val collector = SeasonalEventAchievementCatalog.categoryAchievements(
            SeasonalEventCategory.CORE_SEASONAL,
        ).single()

        assertEquals(SeasonalEventAchievementTier.CATEGORY_COLLECTOR, collector.tier)
        assertEquals(4, collector.targetCount)
        assertEquals(
            SeasonalEventSchema.CategoryAchievementKeys.collector(SeasonalEventCategory.CORE_SEASONAL),
            collector.key,
        )
    }

    @Test
    fun customEventAchievements_useCustomKeys() {
        val achievements = SeasonalEventAchievementCatalog.customEventAchievements(
            eventKey = "spring_festival",
            displayName = "Spring Festival",
        )

        assertEquals(2, achievements.size)
        assertEquals(
            SeasonalEventSchema.CustomAchievementKeys.firstParticipation("spring_festival"),
            achievements[0].key,
        )
        assertEquals(
            SeasonalEventSchema.CustomAchievementKeys.completion("spring_festival"),
            achievements[1].key,
        )
        assertEquals(SeasonalEventCategory.CUSTOM, achievements[0].category)
    }

    @Test
    fun findByKey_resolvesCatalogDefinition() {
        val key = SeasonalEventSchema.AchievementKeys.mastery(SeasonalEventType.WINTER_CRYSTAL)
        val definition = SeasonalEventAchievementCatalog.findByKey(key)

        assertNotNull(definition)
        assertEquals(SeasonalEventAchievementTier.MASTERY, definition?.tier)
        assertTrue(definition!!.targetCount >= 1)
    }
}
