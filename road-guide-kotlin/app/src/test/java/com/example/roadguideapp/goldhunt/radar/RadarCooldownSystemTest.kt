package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarCooldownSystemTest {
    @Test
    fun remainingCooldown_usesRadarTypeCooldownSeconds() {
        val state = RadarTypeCooldownState(
            radarType = RadarType.TREASURE_RADAR,
            lastScanTime = 1_000L,
            scanCount = 2,
            successfulScanCount = 1,
        )
        assertEquals(44_000L, RadarCooldownSystem.remainingCooldownMs(state, 2_000L))
        assertEquals(0L, RadarCooldownSystem.remainingCooldownMs(state, 46_000L))
    }

    @Test
    fun recordTypeScan_incrementsPerTypeCounters() {
        val state = RadarTypeCooldownState.default(RadarType.CLUSTER_RADAR)
        val recorded = RadarCooldownSystem.recordTypeScan(
            state = state,
            successful = true,
            timestampMs = 1_700_000_000_000L,
        )
        assertEquals(1_700_000_000_000L, recorded.lastScanTime)
        assertEquals(1, recorded.scanCount)
        assertEquals(1, recorded.successfulScanCount)
    }

    @Test
    fun snapshot_exposesRemainingCooldownAndScanCount() {
        val state = RadarTypeCooldownState(
            radarType = RadarType.BASIC_RADAR,
            lastScanTime = 0L,
            scanCount = 4,
            successfulScanCount = 2,
        )
        val snapshot = RadarCooldownSystem.snapshot(
            state = state,
            unlockLevel = 3,
            nowMs = 30_000L,
        )
        assertEquals(RadarType.BASIC_RADAR, snapshot.radarType)
        assertEquals(4, snapshot.scanCount)
        assertEquals(60, snapshot.cooldownSeconds)
        assertEquals(30_000L, snapshot.remainingCooldownMs)
        assertFalse(snapshot.canScanNow)
    }

    @Test
    fun perTypeCooldowns_areIndependent() {
        val states = RadarTypeCooldownState.defaults()
        val basicScanned = RadarCooldownSystem.recordTypeScan(
            states.getValue(RadarType.BASIC_RADAR),
            successful = true,
            timestampMs = 1_000L,
        )
        val profile = RadarProfile.default(0L).copy(
            currentRadarType = RadarType.TREASURE_RADAR,
            unlockLevel = 15,
            typeCooldownStates = states + (RadarType.BASIC_RADAR to basicScanned),
        )
        assertEquals(59_000L, profile.cooldownRemainingMs(2_000L, RadarType.BASIC_RADAR))
        assertEquals(0L, profile.cooldownRemainingMs(2_000L, RadarType.TREASURE_RADAR))
        assertTrue(profile.canScanNow(2_000L, RadarType.TREASURE_RADAR))
    }
}
