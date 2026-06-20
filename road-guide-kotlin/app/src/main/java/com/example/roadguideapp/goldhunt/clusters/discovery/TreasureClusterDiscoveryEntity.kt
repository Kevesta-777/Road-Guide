package com.example.roadguideapp.goldhunt.clusters.discovery

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treasure_cluster_discovery")
internal data class TreasureClusterDiscoveryEntity(
    @PrimaryKey val clusterId: String,
    val clusterType: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Double,
    val discoveredAtMs: Long,
    val discoveryTrigger: String,
)
