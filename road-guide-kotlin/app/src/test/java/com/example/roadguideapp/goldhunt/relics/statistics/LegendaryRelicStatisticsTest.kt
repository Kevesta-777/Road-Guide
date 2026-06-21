package com.example.roadguideapp.goldhunt.relics.statistics

import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRevealResult
import org.junit.Assert.assertEquals
import org.junit.Test

class LegendaryRelicStatisticsTest {
    @Test
    fun toHiddenDiscoveryStatistics_mapsUnifiedCounters() {
        val stats = LegendaryRelicStatistics(
            hiddenRelicsDiscovered = 3,
            hiddenRelicsCatalogCount = 10,
        )
        val hidden = stats.toHiddenDiscoveryStatistics()
        assertEquals(10, hidden.hiddenCatalogCount)
        assertEquals(3, hidden.hiddenRevealedCount)
        assertEquals(7, hidden.hiddenUndiscoveredCount)
        assertEquals(0.3f, hidden.revealPercentage, 0.0001f)
    }

    @Test
    fun withHiddenReveal_incrementsDiscoveredCount() {
        val updated = LegendaryRelicStatistics(
            hiddenRelicsDiscovered = 1,
            hiddenRelicsCatalogCount = 4,
        ).withHiddenReveal(
            HiddenRelicDiscoveryRevealResult(
                isNewReveal = true,
                relicId = "legendary_relic_hidden",
            ),
        )
        assertEquals(2, updated.hiddenRelicsDiscovered)
    }
}
