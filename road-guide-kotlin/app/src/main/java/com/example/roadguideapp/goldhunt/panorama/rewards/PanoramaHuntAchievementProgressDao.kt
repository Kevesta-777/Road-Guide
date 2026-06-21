package com.example.roadguideapp.goldhunt.panorama.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
internal interface PanoramaHuntAchievementProgressDao {
    @Query("SELECT * FROM panorama_hunt_achievement_progress WHERE achievementKey = :achievementKey LIMIT 1")
    suspend fun findByKey(achievementKey: String): PanoramaHuntAchievementProgressEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: PanoramaHuntAchievementProgressEntity)

    @Update
    suspend fun update(row: PanoramaHuntAchievementProgressEntity): Int

    @Query("SELECT * FROM panorama_hunt_achievement_progress")
    suspend fun getAll(): List<PanoramaHuntAchievementProgressEntity>
}
