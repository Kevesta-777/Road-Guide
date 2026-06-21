package com.example.roadguideapp.goldhunt.clusters.completion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface TreasureClusterCompletionDao {
    @Query("SELECT clusterId FROM treasure_cluster_completion WHERE clusterId = :clusterId LIMIT 1")
    suspend fun findId(clusterId: String): String?

    @Query("SELECT COUNT(*) FROM treasure_cluster_completion")
    suspend fun count(): Int

    @Query("SELECT COALESCE(SUM(creditsGranted), 0) FROM treasure_cluster_completion")
    suspend fun sumCreditsGranted(): Int

    @Query(
        """
        SELECT clusterType, COUNT(*) AS completedCount
        FROM treasure_cluster_completion
        GROUP BY clusterType
        """,
    )
    suspend fun countByType(): List<ClusterTypeCountRow>

    @Query("SELECT * FROM treasure_cluster_completion ORDER BY completedAtMs ASC")
    suspend fun allOrdered(): List<TreasureClusterCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: TreasureClusterCompletionEntity): Long
}

internal data class ClusterTypeCountRow(
    val clusterType: String,
    val completedCount: Int,
)
