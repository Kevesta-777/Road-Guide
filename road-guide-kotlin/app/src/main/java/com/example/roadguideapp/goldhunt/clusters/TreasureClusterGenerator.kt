package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureHash
import com.example.roadguideapp.map.LatLngBoundsExt
import org.maplibre.android.geometry.LatLngBounds

/**
 * Deterministic procedural generator for [TreasureCluster] instances.
 *
 * Rolls use [TreasureHash] (SHA-256) — never [kotlin.random.Random] or [java.util.Random].
 * Identical [ClusterGenerationRequest] values produce identical clusters after app or device restart.
 */
internal object TreasureClusterGenerator {
    fun generate(request: ClusterGenerationRequest): TreasureCluster? {
        val baseSeed = seedKey(request)
        val spawnRoll = TreasureHash.unitFraction("$baseSeed:spawn")
        if (spawnRoll > ClusterGenerationConfig.PROCEDURAL_SPAWN_CHANCE) {
            return null
        }

        val clusterType = ClusterTypePicker.pick(baseSeed)
        val treasureCount = TreasureHash.intInRange(
            "$baseSeed:count",
            clusterType.minimumTreasureCount,
            clusterType.maximumTreasureCount,
        )
        val radiusSpan = ClusterGenerationConfig.MAX_RADIUS_M - ClusterGenerationConfig.MIN_RADIUS_M
        val radiusMeters = ClusterGenerationConfig.MIN_RADIUS_M +
            TreasureHash.unitFraction("$baseSeed:radius") * radiusSpan
        val clusterSeed = clusterSeedLong(baseSeed)
        val clusterId = TreasureClusterId.forL1Anchor(
            playRegionId = request.playRegionId,
            regionL1Id = request.tile.regionL1Id,
            clusterSeed = clusterSeed,
        )
        val (lat, lng) = ClusterPlacementEngine.placeInTile(
            region = request.region,
            tile = request.tile,
            clusterId = clusterId,
        )
        if (!request.region.contains(lat, lng)) {
            return null
        }

        return TreasureCluster(
            clusterId = clusterId,
            clusterType = clusterType,
            centerLatitude = lat,
            centerLongitude = lng,
            radiusMeters = radiusMeters,
            treasureCount = treasureCount,
            clusterSeed = clusterSeed,
            storyFragmentId = clusterType.storyFragmentKey,
            achievementKey = clusterType.achievementKey,
            legendaryRelicKey = legendaryRelicKeyFor(clusterType, baseSeed),
        )
    }

    fun generateAtTile(
        tile: ClusterTileCoordinate,
        worldSeed: String,
        regionSeed: String,
        playRegionId: String,
        region: PlayRegion,
    ): TreasureCluster? = generate(
        ClusterGenerationRequest(
            tile = tile,
            worldSeed = worldSeed,
            regionSeed = regionSeed,
            playRegionId = playRegionId,
            region = region,
        ),
    )

    fun clustersInBounds(
        bounds: LatLngBounds,
        region: PlayRegion,
        worldSeed: String = ClusterWorldSeed.fromPlayRegion(region),
        regionSeed: String = ClusterRegionSeed.fromPlayRegionId(TreasureGenerator.playRegionId(region)),
        isDiscovered: (String) -> Boolean = { false },
        isClusterCompleted: (String) -> Boolean = { false },
    ): List<TreasureCluster> {
        val playRegionId = TreasureGenerator.playRegionId(region)
        val l1Cells = GridIndex.cellsInBounds(
            bounds = bounds,
            level = 1,
            region = region,
            maxCells = GoldHuntConfig.MAX_TREASURE_L1_CELLS_SCAN,
        )
        val out = ArrayList<TreasureCluster>(l1Cells.size)
        for (l1 in l1Cells) {
            val tile = ClusterTileCoordinate.fromL1Cell(l1)
            val cluster = generateAtTile(
                tile = tile,
                worldSeed = worldSeed,
                regionSeed = regionSeed,
                playRegionId = playRegionId,
                region = region,
            ) ?: continue
            if (isDiscovered(cluster.clusterId) || isClusterCompleted(cluster.clusterId)) continue
            out += cluster
        }
        return out
    }

    fun clustersNear(
        lat: Double,
        lng: Double,
        scanRadiusM: Double,
        region: PlayRegion,
        worldSeed: String = ClusterWorldSeed.fromPlayRegion(region),
        regionSeed: String = ClusterRegionSeed.fromPlayRegionId(TreasureGenerator.playRegionId(region)),
        isDiscovered: (String) -> Boolean = { false },
    ): List<TreasureCluster> {
        val latDelta = scanRadiusM / 111_320.0
        val lngDelta = scanRadiusM / ClusterGeoMath.metersPerDegreeLng(lat)
        val bounds = LatLngBoundsExt.fromEdges(
            south = lat - latDelta,
            west = lng - lngDelta,
            north = lat + latDelta,
            east = lng + lngDelta,
        )
        return clustersInBounds(
            bounds = bounds,
            region = region,
            worldSeed = worldSeed,
            regionSeed = regionSeed,
            isDiscovered = isDiscovered,
        ).filter { cluster ->
            val reachM = scanRadiusM + cluster.radiusMeters
            ClusterGeoMath.distanceMeters(
                lat,
                lng,
                cluster.centerLatitude,
                cluster.centerLongitude,
            ) <= reachM
        }
    }

    internal fun seedKey(request: ClusterGenerationRequest): String =
        listOf(
            request.worldSeed,
            request.regionSeed,
            request.tile.encode(),
            "v${ClusterGenerationConfig.GENERATOR_VERSION}",
        ).joinToString("|")

    private fun clusterSeedLong(baseSeed: String): Long {
        val hi = TreasureHash.digest32("$baseSeed:seed:hi").toLong() and 0xFFFFFFFFL
        val lo = TreasureHash.digest32("$baseSeed:seed:lo").toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    private fun legendaryRelicKeyFor(type: ClusterType, baseSeed: String): String? {
        if (type != ClusterType.LEGENDARY_HOARD) return null
        val roll = TreasureHash.unitFraction("$baseSeed:legendaryRelic")
        return if (roll < 0.35) {
            "${ClusterSchema.ExtensionKeys.LEGENDARY_RELIC}_${type.id.lowercase()}"
        } else {
            null
        }
    }
}
