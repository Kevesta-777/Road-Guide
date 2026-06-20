package com.example.roadguideapp.goldhunt.secretplaces.statistics

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SecretPlaceCategoryStatisticsTest {
    @Test
    fun withDiscovery_incrementsTotalsAndTypeFound() {
        val next = SecretPlaceCategoryStatistics.EMPTY.withDiscovery(
            SecretPlaceCategory.NATURE_SANCTUARY,
        )
        assertEquals(1, next.discoveredCount)
        assertEquals(1, next.natureSanctuariesFound)
        assertEquals(SecretPlaceCategory.NATURE_SANCTUARY, next.bestCategory)
        assertEquals(SecretPlaceCategory.NATURE_SANCTUARY, next.rarestCategory)
    }

    @Test
    fun withCompletion_incrementsTotalsCreditsXpAndType() {
        val next = SecretPlaceCategoryStatistics.EMPTY.withCompletion(
            category = SecretPlaceCategory.LEGENDARY_SITE,
            creditsEarned = 60,
            xpEarned = 225L,
        )
        assertEquals(1, next.completedCount)
        assertEquals(1, next.legendarySitesCompleted)
        assertEquals(60, next.creditsEarned)
        assertEquals(225L, next.xpEarned)
    }

    @Test
    fun resolveBestCategory_prefersMostDiscovered() {
        val stats = SecretPlaceCategoryStatistics.EMPTY
            .withDiscovery(SecretPlaceCategory.NATURE_SANCTUARY)
            .withDiscovery(SecretPlaceCategory.NATURE_SANCTUARY)
            .withDiscovery(SecretPlaceCategory.SCENIC_VIEWPOINT)
        assertEquals(SecretPlaceCategory.NATURE_SANCTUARY, stats.bestCategory)
    }

    @Test
    fun resolveRarestCategory_tracksHighestDifficultyDiscovered() {
        val stats = SecretPlaceCategoryStatistics.EMPTY
            .withDiscovery(SecretPlaceCategory.NATURE_SANCTUARY)
            .withDiscovery(SecretPlaceCategory.LEGENDARY_SITE)
        assertEquals(SecretPlaceCategory.LEGENDARY_SITE, stats.rarestCategory)
    }

    @Test
    fun resolveRarestCategory_nullWhenNothingDiscovered() {
        assertNull(SecretPlaceCategoryStatistics.resolveRarestCategory(SecretPlaceCategoryStatistics.EMPTY))
    }
}
