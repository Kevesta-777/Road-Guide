package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterGenerator
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

internal class TreasureClusterDiscoveryEngine(
    private val gameRepository: GoldHuntRepository,
    private val discoveryRepository: TreasureClusterDiscoveryRepository,
) {
    suspend fun tryDiscoverNear(
        lat: Double,
        lng: Double,
        accuracyMeters: Float?,
        timestampMs: Long = System.currentTimeMillis(),
    ): List<TreasureClusterDiscovery> {
        if (accuracyMeters != null && accuracyMeters > GoldHuntConfig.MAX_LOCATION_ACCURACY_M) {
            return emptyList()
        }
        gameRepository.ensureInitialized()
        discoveryRepository.ensureCacheLoaded()
        val region = gameRepository.playRegion()
        val nearby = TreasureClusterGenerator.clustersNear(
            lat = lat,
            lng = lng,
            scanRadiusM = ClusterDiscoveryConfig.NEARBY_SCAN_RADIUS_M,
            region = region,
            isDiscovered = discoveryRepository::isDiscoveredCached,
        )
        if (nearby.isEmpty()) return emptyList()

        val isExplored = gameRepository::isExploredCached
        val discovered = ArrayList<TreasureClusterDiscovery>(nearby.size)
        for (cluster in nearby) {
            val trigger = ClusterDiscoveryEvaluator.evaluate(
                cluster = cluster,
                playerLat = lat,
                playerLng = lng,
                region = region,
                isExplored = isExplored,
            ) ?: continue
            val result = discoveryRepository.recordDiscovery(
                cluster = cluster,
                trigger = trigger,
                discoveredAtMs = timestampMs,
            )
            if (result.isNew && result.discovery != null) {
                discovered += result.discovery
            }
        }
        return discovered
    }
}
