package com.example.roadguideapp.goldhunt.achievements.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AchievementRewardGrantDao {
    @Query("SELECT achievementId FROM achievement_reward_grant WHERE achievementId = :achievementId LIMIT 1")
    suspend fun findAchievementId(achievementId: String): String?

    @Query("SELECT * FROM achievement_reward_grant ORDER BY grantedAtMs ASC")
    suspend fun getAll(): List<AchievementRewardGrantEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: AchievementRewardGrantEntity): Long
}
