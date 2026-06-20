package com.example.roadguideapp.goldhunt.radar.direction

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarSignalCalculator
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarDirectionalMapperTest {
    @Test
    fun buildWithBearing_populatesDirectionWithoutCoordinates() {
        val signal = requireNotNull(
            RadarSignalCalculator.buildWithBearing(
                targetCategory = RadarTargetCategory.TREASURE,
                targetId = "treasure-directional",
                distanceMeters = 120.0,
                maxRangeMeters = 800,
                playerLat = 51.5,
                playerLng = -0.2,
                targetLat = 51.6,
                targetLng = -0.1,
                detectedTime = 1_700_000_000_000L,
            ),
        )

        assertNotNull(signal.bearingDegrees)
        assertNotNull(signal.compassDirection)
        assertTrue(signal.hasDirectionalHint)
        assertEquals(RadarCompassDirection.NORTH_EAST, signal.compassDirection)
    }

    @Test
    fun fromScanResult_omitsPlayerCoordinates() {
        val signal = requireNotNull(
            RadarSignalCalculator.buildWithBearing(
                targetCategory = RadarTargetCategory.CLUSTER,
                targetId = "cluster-directional",
                distanceMeters = 50.0,
                maxRangeMeters = 1_500,
                playerLat = 51.5,
                playerLng = -0.2,
                targetLat = 51.5,
                targetLng = 0.1,
                detectedTime = 1_700_000_000_000L,
            ),
        )
        val scanResult = RadarScanResult(
            latitude = 51.5,
            longitude = -0.2,
            scannedAtMs = 1_700_000_000_000L,
            radarType = RadarType.CLUSTER_RADAR,
            detectionRangeMeters = 1_500,
            signals = listOf(signal),
        )

        val directional = scanResult.toDirectionalView()

        assertEquals(1, directional.signals.size)
        assertEquals(RadarCompassDirection.EAST, directional.signals.single().compassDirection)
        assertEquals(RadarType.CLUSTER_RADAR, directional.radarType)
    }

    @Test
    fun fromSignal_returnsNullWhenDirectionMissing() {
        val signal = requireNotNull(
            RadarSignalCalculator.build(
                targetCategory = RadarTargetCategory.TREASURE,
                targetId = "no-direction",
                distanceMeters = 10.0,
                maxRangeMeters = 500,
            ),
        )

        assertNull(RadarDirectionalMapper.fromSignal(signal))
    }
}
