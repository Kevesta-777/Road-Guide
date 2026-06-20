package com.example.roadguideapp.goldhunt.secretplaces.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class SecretPlaceCategoryStatisticsMapperTest {
    @Test
    fun entityRoundTrip_preservesCategoryStatistics() {
        val stats = SecretPlaceCategoryStatistics(
            discoveredCount = 5,
            completedCount = 2,
            creditsEarned = 120,
            xpEarned = 340L,
            bestCategory = SecretPlaceCategory.MYSTERY_ZONE,
            rarestCategory = SecretPlaceCategory.LEGENDARY_SITE,
            natureSanctuariesFound = 2,
            scenicViewpointsFound = 1,
            historicLandmarksFound = 1,
            mysteryZonesFound = 1,
            legendarySitesFound = 1,
            mysteryZonesCompleted = 1,
            legendarySitesCompleted = 1,
        )
        val entity = SecretPlaceCategoryStatisticsMapper.applyToEntity(
            ExplorerProfileEntity(),
            stats,
        )
        val restored = SecretPlaceCategoryStatisticsMapper.fromEntity(entity)
        assertEquals(stats, restored)
    }
}
