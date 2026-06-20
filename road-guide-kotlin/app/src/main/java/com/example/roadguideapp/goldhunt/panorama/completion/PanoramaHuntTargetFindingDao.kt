package com.example.roadguideapp.goldhunt.panorama.completion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface PanoramaHuntTargetFindingDao {
    @Query("SELECT slotIndex FROM panorama_hunt_target_finding WHERE huntId = :huntId ORDER BY slotIndex ASC")
    suspend fun slotIndicesForHunt(huntId: String): List<Int>

    @Query("SELECT COUNT(*) FROM panorama_hunt_target_finding WHERE huntId = :huntId")
    suspend fun countForHunt(huntId: String): Int

    @Query("SELECT targetId FROM panorama_hunt_target_finding WHERE targetId = :targetId LIMIT 1")
    suspend fun findTargetId(targetId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: PanoramaHuntTargetFindingEntity): Long
}
