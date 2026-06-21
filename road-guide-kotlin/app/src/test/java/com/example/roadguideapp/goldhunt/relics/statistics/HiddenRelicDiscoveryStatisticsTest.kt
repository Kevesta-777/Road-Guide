package com.example.roadguideapp.goldhunt.relics.statistics

import org.junit.Assert.assertEquals
import org.junit.Test

class HiddenRelicDiscoveryStatisticsTest {
    @Test
    fun revealPercentage_derivesFromCounts() {
        val stats = HiddenRelicDiscoveryStatistics(
            hiddenCatalogCount = 8,
            hiddenRevealedCount = 2,
            hiddenUndiscoveredCount = 6,
        )
        assertEquals(0.25f, stats.revealPercentage, 0.0001f)
    }

    @Test
    fun emptyStats_returnZeroPercentage() {
        assertEquals(0f, HiddenRelicDiscoveryStatistics.EMPTY.revealPercentage, 0.0001f)
    }
}
