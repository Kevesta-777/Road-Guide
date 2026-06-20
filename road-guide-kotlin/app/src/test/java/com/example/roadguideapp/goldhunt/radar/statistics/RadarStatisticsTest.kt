package com.example.roadguideapp.goldhunt.radar.statistics

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarSignalFactory
import com.example.roadguideapp.goldhunt.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Test

class RadarStatisticsTest {
    @Test
    fun withScan_incrementsScanAndDetectionCounters() {
        val scannedAtMs = 1_700_000_001_000L
        val treasure = requireNotNull(
            RadarSignalFactory.treasure("t-stat", 0.0, 800, scannedAtMs),
        )
        val story = requireNotNull(
            RadarSignalFactory.storyFragment(
                "story_fragment_historic_landmark",
                120.0,
                2_000,
                scannedAtMs,
            ),
        )
        val scanResult = RadarScanResult(
            latitude = 48.1,
            longitude = 11.5,
            scannedAtMs = scannedAtMs,
            radarType = RadarType.STORY_RADAR,
            detectionRangeMeters = 2_000,
            signals = listOf(treasure, story),
        )

        val next = RadarStatistics.EMPTY.withScan(scanResult)

        assertEquals(1, next.totalScans)
        assertEquals(1, next.successfulScans)
        assertEquals(1, next.treasuresFound)
        assertEquals(0, next.clustersFound)
        assertEquals(0, next.secretPlacesFound)
        assertEquals(1, next.storyDiscoveries)
        assertEquals(0, next.legendaryDiscoveries)
        assertEquals(1.0, next.successRate, 0.0001)
    }

    @Test
    fun withScan_emptyPulseCountsAsUnsuccessful() {
        val scannedAtMs = 1_700_000_002_000L
        val scanResult = RadarScanResult(
            latitude = 48.1,
            longitude = 11.5,
            scannedAtMs = scannedAtMs,
            radarType = RadarType.BASIC_RADAR,
            detectionRangeMeters = 800,
            signals = emptyList(),
        )

        val next = RadarStatistics.EMPTY.withScan(scanResult)

        assertEquals(1, next.totalScans)
        assertEquals(0, next.successfulScans)
        assertEquals(0.0, next.successRate, 0.0001)
    }
}
