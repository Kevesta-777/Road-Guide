package com.example.roadguideapp.goldhunt.clusters.discovery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface TreasureClusterDiscoveryDao {
    @Query("SELECT clusterId FROM treasure_cluster_discovery WHERE clusterId = :clusterId LIMIT 1")
    suspend fun findId(clusterId: String): String?

    @Query("SELECT COUNT(*) FROM treasure_cluster_discovery")
    suspend fun count(): Int

    @Query(
        """
        SELECT clusterType, COUNT(*) AS discoveredCount
        FROM treasure_cluster_discovery
        GROUP BY clusterType
        """,
    )
    suspend fun countByType(): List<ClusterDiscoveryTypeCountRow>

    @Query("SELECT * FROM treasure_cluster_discovery ORDER BY discoveredAtMs ASC")
    suspend fun allOrdered(): List<TreasureClusterDiscoveryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: TreasureClusterDiscoveryEntity): Long
}

internal data class ClusterDiscoveryTypeCountRow(
    val clusterType: String,
    val discoveredCount: Int,
)
