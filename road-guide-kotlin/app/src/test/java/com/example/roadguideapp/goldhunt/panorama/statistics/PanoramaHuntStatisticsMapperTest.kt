package com.example.roadguideapp.goldhunt.panorama.statistics

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfile
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class PanoramaHuntStatisticsMapperTest {
    @Test
    fun profileRoundTrip_preservesPanoramaStats() {
        val stats = PanoramaHuntStatistics(
            totalHunts = 5,
            huntsCompleted = 3,
            targetsFound = 7,
            rewardsEarned = 120,
            xpEarned = 240L,
            hiddenSymbolsFound = 2,
            objectsFound = 1,
            codesSolved = 1,
            panoramaPuzzlesSolved = 1,
            relicHuntsCompleted = 0,
        )
        val profile = ExplorerProfile.default().copy(
            totalPanoramaHuntsCompleted = stats.huntsCompleted,
            panoramaHuntStatistics = stats,
        )
        val entity = ExplorerProfileMapper.toEntity(profile)
        assertEquals(5, entity.panoramaHuntStatsTotalHunts)
        assertEquals(3, entity.totalPanoramaHuntsCompleted)
        assertEquals(7, entity.panoramaHuntStatsTargetsFound)
        assertEquals(120, entity.panoramaHuntStatsRewardsEarned)
        assertEquals(
            stats,
            ExplorerProfileMapper.toDomain(entity).panoramaHuntStatistics,
        )
    }

    @Test
    fun withTargetFound_incrementsSymbolCounter() {
        val updated = PanoramaHuntStatistics.EMPTY.withTargetFound(PanoramaHiddenTargetType.SYMBOL)
        assertEquals(1, updated.hiddenSymbolsFound)
        assertEquals(1, updated.targetsFound)
    }

    @Test
    fun withHuntCompletion_incrementsCompletionAndRewards() {
        val updated = PanoramaHuntStatistics.EMPTY.withHuntCompletion(
            creditsEarned = 50,
            xpEarned = 80L,
        )
        assertEquals(1, updated.huntsCompleted)
        assertEquals(50, updated.rewardsEarned)
        assertEquals(0, updated.codesSolved)
    }

    @Test
    fun applyToEntity_mapsAllFields() {
        val stats = PanoramaHuntStatistics(
            totalHunts = 2,
            huntsCompleted = 1,
            hiddenSymbolsFound = 3,
            objectsFound = 4,
            codesSolved = 5,
            rewardsEarned = 90,
        )
        val entity = PanoramaHuntStatisticsMapper.applyToEntity(
            ExplorerProfileMapper.toEntity(ExplorerProfile.default()),
            stats,
        )
        assertEquals(2, entity.panoramaHuntStatsTotalHunts)
        assertEquals(3, entity.panoramaHuntStatsHiddenSymbolsFound)
        assertEquals(4, entity.panoramaHuntStatsObjectsFound)
        assertEquals(5, entity.panoramaHuntStatsCodesSolved)
        assertEquals(90, entity.panoramaHuntStatsRewardsEarned)
    }
}
