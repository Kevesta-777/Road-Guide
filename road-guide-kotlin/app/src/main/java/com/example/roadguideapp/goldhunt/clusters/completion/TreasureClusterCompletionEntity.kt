package com.example.roadguideapp.goldhunt.clusters.completion

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.roadguideapp.goldhunt.clusters.ClusterSchema

@Entity(tableName = "treasure_cluster_completion")
internal data class TreasureClusterCompletionEntity(
    @PrimaryKey val clusterId: String,
    val clusterType: String,
    val treasureCount: Int,
    val creditsGranted: Int,
    val xpGranted: Long,
    val completedAtMs: Long,
    val achievementKey: String?,
    val storyFragmentId: String?,
    val legendaryRelicKey: String?,
    val schemaVersion: Int = ClusterSchema.VERSION,
    val extensionJson: String = ClusterSchema.EMPTY_EXTENSIONS_JSON,
)
