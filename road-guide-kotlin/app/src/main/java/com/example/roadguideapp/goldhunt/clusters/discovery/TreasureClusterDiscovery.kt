package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal data class TreasureClusterDiscovery(
    val clusterId: String,
    val clusterType: ClusterType,
    val discoveredAtMs: Long,
    val discoveryTrigger: ClusterDiscoveryTrigger,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Double,
)

internal data class ClusterDiscoveryRecordResult(
    val discovery: TreasureClusterDiscovery?,
    val isNew: Boolean,
) {
    companion object {
        val skipped = ClusterDiscoveryRecordResult(discovery = null, isNew = false)
    }
}
