package com.example.roadguideapp.goldhunt.panorama.statistics

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType
import org.junit.Assert.assertEquals
import org.junit.Test

class PanoramaHuntStatisticsTest {
    @Test
    fun withHuntStarted_incrementsTotalHunts() {
        val updated = PanoramaHuntStatistics.EMPTY.withHuntStarted()
        assertEquals(1, updated.totalHunts)
    }

    @Test
    fun withTargetFound_incrementsTypeCounters() {
        val updated = PanoramaHuntStatistics.EMPTY
            .withTargetFound(PanoramaHiddenTargetType.SYMBOL)
            .withTargetFound(PanoramaHiddenTargetType.OBJECT)
            .withTargetFound(PanoramaHiddenTargetType.CODE_RUNE)
        assertEquals(3, updated.targetsFound)
        assertEquals(1, updated.hiddenSymbolsFound)
        assertEquals(1, updated.objectsFound)
        assertEquals(1, updated.codesSolved)
    }

    @Test
    fun withHuntCompletion_incrementsCompletionAndRewardsOnly() {
        val updated = PanoramaHuntStatistics.EMPTY.withHuntCompletion(
            creditsEarned = 75,
            xpEarned = 120L,
        )
        assertEquals(1, updated.huntsCompleted)
        assertEquals(75, updated.rewardsEarned)
        assertEquals(120L, updated.xpEarned)
        assertEquals(0, updated.hiddenSymbolsFound)
        assertEquals(0, updated.codesSolved)
    }

    @Test
    fun completedCountFor_mapsHuntTypeToFindingCounters() {
        val stats = PanoramaHuntStatistics(
            hiddenSymbolsFound = 2,
            objectsFound = 3,
            codesSolved = 1,
            panoramaPuzzlesSolved = 4,
            relicHuntsCompleted = 5,
        )
        assertEquals(2, stats.completedCountFor(PanoramaHuntType.HIDDEN_SYMBOL))
        assertEquals(3, stats.completedCountFor(PanoramaHuntType.OBJECT_HUNT))
        assertEquals(1, stats.completedCountFor(PanoramaHuntType.SECRET_CODE))
        assertEquals(4, stats.completedCountFor(PanoramaHuntType.PANORAMA_PUZZLE))
        assertEquals(5, stats.completedCountFor(PanoramaHuntType.RELIC_HUNT))
    }
}
