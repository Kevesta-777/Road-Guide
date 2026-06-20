package com.example.roadguideapp.goldhunt.panorama.completion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface PanoramaHuntCompletionDao {
    @Query("SELECT huntId FROM panorama_hunt_completion WHERE huntId = :huntId LIMIT 1")
    suspend fun findHuntId(huntId: String): String?

    @Query("SELECT COUNT(*) FROM panorama_hunt_completion")
    suspend fun count(): Int

    @Query("SELECT COALESCE(SUM(creditsGranted), 0) FROM panorama_hunt_completion")
    suspend fun sumCreditsGranted(): Int

    @Query("SELECT COALESCE(SUM(xpGranted), 0) FROM panorama_hunt_completion")
    suspend fun sumXpGranted(): Long

    @Query(
        """
        SELECT huntType, COUNT(*) AS completedCount
        FROM panorama_hunt_completion
        GROUP BY huntType
        """,
    )
    suspend fun countByType(): List<PanoramaHuntTypeCountRow>

    @Query("SELECT * FROM panorama_hunt_completion WHERE huntId = :huntId LIMIT 1")
    suspend fun findByHuntId(huntId: String): PanoramaHuntCompletionEntity?

    @Query("SELECT * FROM panorama_hunt_completion ORDER BY completedAtMs ASC")
    suspend fun allOrdered(): List<PanoramaHuntCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: PanoramaHuntCompletionEntity): Long
}

internal data class PanoramaHuntTypeCountRow(
    val huntType: String,
    val completedCount: Int,
)
