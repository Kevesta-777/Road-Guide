package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEventCategoryTest {
    @Test
    fun forType_mapsKnownFamilies() {
        assertEquals(
            SeasonalEventCategory.CORE_SEASONAL,
            SeasonalEventCategory.forType(SeasonalEventType.SPRING_BLOSSOM),
        )
        assertEquals(
            SeasonalEventCategory.HOLIDAY,
            SeasonalEventCategory.forType(SeasonalEventType.HALLOWEEN_MYSTERY),
        )
        assertEquals(
            SeasonalEventCategory.SPECIAL,
            SeasonalEventCategory.forType(SeasonalEventType.COMMUNITY_CHALLENGE),
        )
    }

    @Test
    fun eventTypesInCategory_listsAllMembers() {
        val holidayTypes = SeasonalEventAchievementCatalog.eventTypesInCategory(
            SeasonalEventCategory.HOLIDAY,
        )

        assertEquals(2, holidayTypes.size)
        assertTrue(holidayTypes.contains(SeasonalEventType.HALLOWEEN_MYSTERY))
        assertTrue(holidayTypes.contains(SeasonalEventType.NEW_YEAR_GOLDEN_HUNT))
    }
}
