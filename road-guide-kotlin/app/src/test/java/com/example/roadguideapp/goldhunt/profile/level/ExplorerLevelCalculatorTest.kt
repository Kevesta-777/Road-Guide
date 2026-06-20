package com.example.roadguideapp.goldhunt.profile.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplorerLevelCalculatorTest {
    @Test
    fun xpRequiredForLevel_anchorMilestones_matchDesign() {
        assertEquals(0L, ExplorerLevelCalculator.xpRequiredForLevel(1))
        assertEquals(100L, ExplorerLevelCalculator.xpRequiredForLevel(2))
        assertEquals(250L, ExplorerLevelCalculator.xpRequiredForLevel(3))
        assertEquals(1_500L, ExplorerLevelCalculator.xpRequiredForLevel(5))
        assertEquals(10_000L, ExplorerLevelCalculator.xpRequiredForLevel(10))
        assertEquals(50_000L, ExplorerLevelCalculator.xpRequiredForLevel(20))
    }

    @Test
    fun xpRequiredForLevel_monotonicThroughLevel100() {
        var previous = -1L
        for (level in 1..100) {
            val threshold = ExplorerLevelCalculator.xpRequiredForLevel(level)
            assertTrue("level $level should be >= previous", threshold >= previous)
            previous = threshold
        }
    }

    @Test
    fun calculateLevel_respectsAnchorThresholds() {
        assertEquals(1, ExplorerLevelCalculator.calculateLevel(0L))
        assertEquals(1, ExplorerLevelCalculator.calculateLevel(99L))
        assertEquals(2, ExplorerLevelCalculator.calculateLevel(100L))
        assertEquals(2, ExplorerLevelCalculator.calculateLevel(249L))
        assertEquals(3, ExplorerLevelCalculator.calculateLevel(250L))
        assertEquals(5, ExplorerLevelCalculator.calculateLevel(1_500L))
        assertEquals(10, ExplorerLevelCalculator.calculateLevel(10_000L))
        assertEquals(20, ExplorerLevelCalculator.calculateLevel(50_000L))
    }

    @Test
    fun xpProgressToNextLevel_atLevelStart_isZero() {
        val progress = ExplorerLevelCalculator.xpProgressToNextLevel(100L)
        assertEquals(2, progress.currentLevel)
        assertEquals(0L, progress.xpIntoLevel)
        assertEquals(150L, progress.xpToNextLevel)
        assertEquals(0.0, progress.progressFraction, 0.0001)
    }

    @Test
    fun xpProgressToNextLevel_midBand_isHalf() {
        val floor = ExplorerLevelCalculator.xpRequiredForLevel(2)
        val ceiling = ExplorerLevelCalculator.xpRequiredForLevel(3)
        val midXp = floor + (ceiling - floor) / 2
        val progress = ExplorerLevelCalculator.xpProgressToNextLevel(midXp)
        assertEquals(2, progress.currentLevel)
        assertEquals(0.5, progress.progressFraction, 0.02)
    }

    @Test
    fun xpRequiredForLevel_beyond100_remainsScalable() {
        val level150 = ExplorerLevelCalculator.xpRequiredForLevel(150)
        val level200 = ExplorerLevelCalculator.xpRequiredForLevel(200)
        assertTrue(level200 > level150)
        assertTrue(level150 > ExplorerLevelCalculator.xpRequiredForLevel(100))
    }
}
