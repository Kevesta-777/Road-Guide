package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Test

class SeasonalEventCatalogTest {
    @Test
    fun displayOrder_containsAllEightEvents() {
        assertEquals(8, SeasonalEventCatalog.displayOrder.size)
        assertEquals(
            SeasonalEventType.ALL_ORDERED,
            SeasonalEventCatalog.displayOrder,
        )
    }

    @Test
    fun fromId_roundTripsEnumName() {
        assertEquals(
            SeasonalEventType.COMMUNITY_CHALLENGE,
            SeasonalEventCatalog.fromId("COMMUNITY_CHALLENGE"),
        )
    }

    @Test
    fun templateFor_preservesMultiplierAndWindow() {
        val template = SeasonalEventCatalog.templateFor(SeasonalEventType.AUTUMN_HARVEST)

        assertEquals("Autumn Harvest", template.displayName)
        assertEquals(9, template.startMonth)
        assertEquals(1, template.startDay)
        assertEquals(11, template.endMonth)
        assertEquals(30, template.endDay)
        assertEquals(1.25, template.eventMultiplier, 0.0001)
    }
}
