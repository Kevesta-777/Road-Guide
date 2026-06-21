package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarProfileReconcilerTest {
    private fun profile(
        type: RadarType = RadarType.BASIC_RADAR,
        unlockLevel: Int = 1,
        lastScanTime: Long? = null,
        totalScans: Int = 0,
        successfulScans: Int = 0,
        typeCooldownStates: Map<RadarType, RadarTypeCooldownState>? = null,
    ): RadarProfile {
        val states = typeCooldownStates ?: RadarTypeCooldownState.defaults().let { defaults ->
            if (lastScanTime == null) {
                defaults
            } else {
                defaults + (type to defaults.getValue(type).copy(
                    lastScanTime = lastScanTime,
                    scanCount = totalScans,
                    successfulScanCount = successfulScans,
                ))
            }
        }
        return RadarProfile(
            currentRadarType = type,
            unlockLevel = unlockLevel,
            lastScanTime = lastScanTime,
            totalScans = totalScans,
            successfulScans = successfulScans,
            typeCooldownStates = states,
        )
    }

    @Test
    fun reconcileWithExplorerLevel_updatesUnlockAndClampsRadar() {
        val current = profile(type = RadarType.LEGENDARY_RADAR, unlockLevel = 1)
        val reconciled = RadarProfileReconciler.reconcileWithExplorerLevel(current, 7)

        assertEquals(7, reconciled.unlockLevel)
        assertEquals(RadarType.CLUSTER_RADAR, reconciled.currentRadarType)
    }

    @Test
    fun equipRadar_blocksWhenLevelTooLow() {
        val result = RadarProfileReconciler.equipRadar(
            profile = profile(unlockLevel = 2),
            radarType = RadarType.TREASURE_RADAR,
        )
        assertTrue(result is RadarProfileEquipResult.Blocked)
        assertEquals(1, (result as RadarProfileEquipResult.Blocked).levelsUntilUnlocked)
    }

    @Test
    fun equipRadar_succeedsWhenUnlocked() {
        val result = RadarProfileReconciler.equipRadar(
            profile = profile(unlockLevel = 5),
            radarType = RadarType.SECRET_PLACE_RADAR,
        )
        assertTrue(result is RadarProfileEquipResult.Equipped)
        assertEquals(
            RadarType.SECRET_PLACE_RADAR,
            (result as RadarProfileEquipResult.Equipped).profile.currentRadarType,
        )
    }

    @Test
    fun recordScan_incrementsCountersAndSetsLastScanTime() {
        val timestamp = 1_700_000_000_000L
        val result = RadarProfileReconciler.recordScan(
            profile = profile(unlockLevel = 3, totalScans = 2, successfulScans = 1),
            successful = true,
            timestampMs = timestamp,
        )
        assertTrue(result is RadarProfileRecordScanResult.Recorded)
        val recorded = result as RadarProfileRecordScanResult.Recorded
        assertEquals(timestamp, recorded.profile.lastScanTime)
        assertEquals(3, recorded.profile.totalScans)
        assertEquals(2, recorded.profile.successfulScans)
        assertFalse(recorded.isFirstScan)
        assertFalse(recorded.isFirstSuccessfulScan)
    }

    @Test
    fun recordScan_respectsCooldown() {
        val lastScan = 1_700_000_000_000L
        val result = RadarProfileReconciler.recordScan(
            profile = profile(
                unlockLevel = 3,
                lastScanTime = lastScan,
                totalScans = 1,
            ),
            successful = true,
            timestampMs = lastScan + 1_000L,
        )
        assertTrue(result is RadarProfileRecordScanResult.CooldownActive)
    }

    @Test
    fun recordScan_firstScanFlags() {
        val result = RadarProfileReconciler.recordScan(
            profile = profile(unlockLevel = 3),
            successful = true,
            timestampMs = 1_700_000_000_000L,
        )
        assertTrue(result is RadarProfileRecordScanResult.Recorded)
        val recorded = result as RadarProfileRecordScanResult.Recorded
        assertTrue(recorded.isFirstScan)
        assertTrue(recorded.isFirstSuccessfulScan)
        assertTrue(recorded.isFirstScanForType)
        assertTrue(recorded.isFirstSuccessfulScanForType)
    }

    @Test
    fun recordScan_updatesPerTypeStateIndependently() {
        val timestamp = 1_700_000_000_000L
        val result = RadarProfileReconciler.recordScan(
            profile = profile(
                type = RadarType.TREASURE_RADAR,
                unlockLevel = 5,
                totalScans = 2,
                successfulScans = 1,
            ),
            successful = false,
            timestampMs = timestamp,
        )
        assertTrue(result is RadarProfileRecordScanResult.Recorded)
        val recorded = result as RadarProfileRecordScanResult.Recorded
        val typeState = recorded.profile.typeCooldownState(RadarType.TREASURE_RADAR)
        assertEquals(timestamp, typeState.lastScanTime)
        assertEquals(1, typeState.scanCount)
        assertEquals(0, typeState.successfulScanCount)
        assertEquals(3, recorded.profile.totalScans)
    }
}
