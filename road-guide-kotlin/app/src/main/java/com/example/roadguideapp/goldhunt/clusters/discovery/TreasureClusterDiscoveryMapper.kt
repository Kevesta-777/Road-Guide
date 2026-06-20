package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster

internal object TreasureClusterDiscoveryMapper {
    fun toEntity(
        cluster: TreasureCluster,
        trigger: ClusterDiscoveryTrigger,
        discoveredAtMs: Long,
    ): TreasureClusterDiscoveryEntity = TreasureClusterDiscoveryEntity(
        clusterId = cluster.clusterId,
        clusterType = cluster.clusterType.id,
        centerLatitude = cluster.centerLatitude,
        centerLongitude = cluster.centerLongitude,
        radiusMeters = cluster.radiusMeters,
        discoveredAtMs = discoveredAtMs,
        discoveryTrigger = trigger.id,
    )

    fun toDomain(entity: TreasureClusterDiscoveryEntity): TreasureClusterDiscovery? {
        val type = ClusterType.fromId(entity.clusterType) ?: return null
        val trigger = ClusterDiscoveryTrigger.fromId(entity.discoveryTrigger) ?: return null
        return TreasureClusterDiscovery(
            clusterId = entity.clusterId,
            clusterType = type,
            discoveredAtMs = entity.discoveredAtMs,
            discoveryTrigger = trigger,
            centerLatitude = entity.centerLatitude,
            centerLongitude = entity.centerLongitude,
            radiusMeters = entity.radiusMeters,
        )
    }
}
