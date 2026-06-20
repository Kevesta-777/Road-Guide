package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarExplorerLevelGateTest {
    @Test
    fun isUnlocked_respectsUnlockLevel() {
        assertTrue(RadarExplorerLevelGate.isUnlocked(RadarType.BASIC_RADAR, 1))
        assertFalse(RadarExplorerLevelGate.isUnlocked(RadarType.TREASURE_RADAR, 2))
        assertTrue(RadarExplorerLevelGate.isUnlocked(RadarType.TREASURE_RADAR, 3))
        assertFalse(RadarExplorerLevelGate.isUnlocked(RadarType.LEGENDARY_RADAR, 14))
        assertTrue(RadarExplorerLevelGate.isUnlocked(RadarType.LEGENDARY_RADAR, 15))
    }

    @Test
    fun levelsUntilUnlocked_countsRemainingLevels() {
        assertEquals(0, RadarExplorerLevelGate.levelsUntilUnlocked(RadarType.BASIC_RADAR, 5))
        assertEquals(2, RadarExplorerLevelGate.levelsUntilUnlocked(RadarType.CLUSTER_RADAR, 5))
        assertEquals(5, RadarExplorerLevelGate.levelsUntilUnlocked(RadarType.LEGENDARY_RADAR, 10))
    }

    @Test
    fun unlockedTypes_returnsAllEligibleRadars() {
        val unlocked = RadarExplorerLevelGate.unlockedTypes(7)
        assertEquals(
            listOf(
                RadarType.BASIC_RADAR,
                RadarType.TREASURE_RADAR,
                RadarType.SECRET_PLACE_RADAR,
                RadarType.CLUSTER_RADAR,
            ),
            unlocked,
        )
    }
}
