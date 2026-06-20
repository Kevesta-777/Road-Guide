package com.example.roadguideapp.goldhunt.achievements.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AchievementRewardHookDao {
    @Query("SELECT eventId FROM achievement_reward_hook WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: AchievementRewardHookEntity): Long

    @Query("SELECT COUNT(*) FROM achievement_reward_hook WHERE hookType = :hookType")
    suspend fun countByHookType(hookType: String): Int

    @Query(
        """
        SELECT * FROM achievement_reward_hook
        WHERE hookType = :hookType
        ORDER BY grantedAtMs DESC
        """,
    )
    suspend fun getAllByHookType(hookType: String): List<AchievementRewardHookEntity>
}
