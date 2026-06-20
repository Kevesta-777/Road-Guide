package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.clusters.ClusterTileCoordinate
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterGenerator
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarScanEngineTest {
    private val region = PlayRegion(
        west = -0.5,
        south = 51.4,
        east = 0.1,
        north = 51.6,
        totalCellsL0 = 1_000_000L,
    )
    private val playRegionId = TreasureGenerator.playRegionId(region)
    private val scanLat = 51.5
    private val scanLng = -0.2
    private val scannedAtMs = 1_700_000_000_000L

    private fun profile(type: RadarType = RadarType.BASIC_RADAR) = RadarProfile(
        currentRadarType = type,
        unlockLevel = 15,
        lastScanTime = null,
        totalScans = 0,
        successfulScans = 0,
    )

    private fun context(
        radarType: RadarType = RadarType.BASIC_RADAR,
        rangeMeters: Int = 3_000,
        secretPlacesUnlocked: Boolean = true,
    ) = RadarScanContext(
        playRegion = region,
        playRegionId = playRegionId,
        detectionRangeMeters = rangeMeters,
        radarType = radarType,
        isTreasureCollected = { false },
        isTreasurePlacementExplored = { _, _ -> true },
        isSecretPlaceDiscovered = { false },
        areSecretPlacesUnlocked = secretPlacesUnlocked,
        isClusterCompleted = { false },
        treasureGenerationTier = TreasureGenerationTier.STAR_ONLY,
        activeHoard = null,
    )

    @Test
    fun scan_basicRadar_detectsTreasuresOnly() {
        val result = RadarScanEngine.scan(
            latitude = scanLat,
            longitude = scanLng,
            profile = profile(RadarType.BASIC_RADAR),
            context = context(radarType = RadarType.BASIC_RADAR),
            scannedAtMs = scannedAtMs,
        )

        assertEquals(scanLat, result.latitude, 0.0001)
        assertEquals(scanLng, result.longitude, 0.0001)
        assertEquals(RadarType.BASIC_RADAR, result.radarType)
        assertTrue(result.signals.all { it.targetCategory == RadarTargetCategory.TREASURE })
        assertTrue(result.signals.all { it.detectedTime == scannedAtMs })
        result.signals.forEach { signal ->
            assertTrue(signal.hasDirectionalHint)
        }
        assertEquals(
            result.signals.size,
            result.toDirectionalView().signals.size,
        )
    }

    @Test
    fun scan_clusterRadar_usesDistanceStrengthAtCenter() {
        val cluster = requireNotNull(findSpawnedCluster())
        val signal = RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.CLUSTER,
            targetId = cluster.clusterId,
            distanceMeters = 0.0,
            maxRangeMeters = RadarType.CLUSTER_RADAR.detectionRangeMeters,
            detectedTime = scannedAtMs,
            radarType = RadarType.CLUSTER_RADAR,
        )
        assertNotNull(signal)
        assertEquals(1.0, signal!!.signalStrength, 0.0001)
        assertEquals(RadarSignalType.IMMEDIATE, signal.signalType)
    }

    @Test
    fun scan_storyRadar_includesStoryFragmentsWhenPresent() {
        val result = RadarScanEngine.scan(
            latitude = scanLat,
            longitude = scanLng,
            profile = profile(RadarType.STORY_RADAR),
            context = context(radarType = RadarType.STORY_RADAR),
            scannedAtMs = scannedAtMs,
        )

        val categories = result.signals.map { it.targetCategory }.toSet()
        assertTrue(
            categories.contains(RadarTargetCategory.STORY_FRAGMENT) ||
                categories.contains(RadarTargetCategory.SECRET_PLACE),
        )
    }

    @Test
    fun scan_secretPlacesBlockedWhenLocked() {
        val result = RadarScanEngine.scan(
            latitude = scanLat,
            longitude = scanLng,
            profile = profile(RadarType.SECRET_PLACE_RADAR),
            context = context(
                radarType = RadarType.SECRET_PLACE_RADAR,
                secretPlacesUnlocked = false,
            ),
            scannedAtMs = scannedAtMs,
        )

        assertFalse(result.signals.any { it.targetCategory == RadarTargetCategory.SECRET_PLACE })
    }

    @Test
    fun scan_signalsAreSortedByPriorityAndStrength() {
        val result = RadarScanEngine.scan(
            latitude = scanLat,
            longitude = scanLng,
            profile = profile(RadarType.LEGENDARY_RADAR),
            context = context(radarType = RadarType.LEGENDARY_RADAR),
            scannedAtMs = scannedAtMs,
        )

        if (result.signals.size >= 2) {
            val firstWeight = RadarDetectionScope.priorityWeight(
                result.signals[0].targetCategory,
                RadarType.LEGENDARY_RADAR,
            )
            val secondWeight = RadarDetectionScope.priorityWeight(
                result.signals[1].targetCategory,
                RadarType.LEGENDARY_RADAR,
            )
            assertTrue(firstWeight >= secondWeight)
        }
        result.signals.forEach { signal ->
            assertNotNull(signal.signalType)
            assertTrue(signal.signalStrength in 0.0..1.0)
            assertTrue(signal.distanceMeters >= 0.0)
            assertTrue(signal.targetId.isNotBlank())
        }
    }

    private fun findSpawnedCluster(): com.example.roadguideapp.goldhunt.clusters.TreasureCluster? {
        for (x in 0 until 40) {
            for (y in 0 until 40) {
                val cluster = TreasureClusterGenerator.generateAtTile(
                    tile = ClusterTileCoordinate(level = 1, tileX = x, tileY = y),
                    worldSeed = com.example.roadguideapp.goldhunt.clusters.ClusterWorldSeed.DEFAULT,
                    regionSeed = com.example.roadguideapp.goldhunt.clusters.ClusterRegionSeed.fromPlayRegionId(
                        playRegionId,
                    ),
                    playRegionId = playRegionId,
                    region = region,
                )
                if (cluster != null) return cluster
            }
        }
        return null
    }
}
