package com.example.roadguideapp.goldhunt.panorama.statistics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface PanoramaHuntSessionDao {
    @Query("SELECT huntId FROM panorama_hunt_session WHERE huntId = :huntId LIMIT 1")
    suspend fun findHuntId(huntId: String): String?

    @Query("SELECT COUNT(*) FROM panorama_hunt_session")
    suspend fun count(): Int

    @Query("SELECT * FROM panorama_hunt_session ORDER BY startedAtMs ASC")
    suspend fun allOrdered(): List<PanoramaHuntSessionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: PanoramaHuntSessionEntity): Long
}
