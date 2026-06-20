package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarProfileTest {
    @Test
    fun default_startsWithBasicRadar() {
        val profile = RadarProfile.default(100L)
        assertEquals(RadarType.BASIC_RADAR, profile.currentRadarType)
        assertEquals(RadarSchema.DEFAULT_UNLOCK_LEVEL, profile.unlockLevel)
        assertNull(profile.lastScanTime)
        assertEquals(0, profile.totalScans)
        assertEquals(0, profile.successfulScans)
        assertEquals(100L, profile.createdAtMs)
    }

    @Test
    fun canScanNow_trueForFreshProfile() {
        val profile = RadarProfile.default()
        assertTrue(profile.canScanNow())
    }

    @Test
    fun cooldownRemainingMs_zeroWhenNeverScanned() {
        val profile = RadarProfile.default()
        assertEquals(0L, profile.cooldownRemainingMs())
    }

    @Test
    fun successRate_zeroWhenNoScans() {
        val profile = RadarProfile.default()
        assertEquals(0.0, profile.successRate, 0.0001)
    }

    @Test
    fun isCurrentRadarUnlocked_falseWhenEquippedAboveLevel() {
        val profile = RadarProfile(
            currentRadarType = RadarType.LEGENDARY_RADAR,
            unlockLevel = 5,
            lastScanTime = null,
            totalScans = 0,
            successfulScans = 0,
        )
        assertFalse(profile.isCurrentRadarUnlocked)
    }
}
