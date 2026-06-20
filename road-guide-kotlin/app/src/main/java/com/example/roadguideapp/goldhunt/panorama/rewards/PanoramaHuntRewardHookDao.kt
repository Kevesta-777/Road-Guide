package com.example.roadguideapp.goldhunt.panorama.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface PanoramaHuntRewardHookDao {
    @Query("SELECT eventId FROM panorama_hunt_reward_hook WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Query(
        """
        SELECT eventId FROM panorama_hunt_reward_hook
        WHERE hookKey = :hookKey AND hookType = :hookType
        LIMIT 1
        """,
    )
    suspend fun findHook(hookKey: String, hookType: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: PanoramaHuntRewardHookEntity): Long

    @Query("SELECT COUNT(*) FROM panorama_hunt_reward_hook WHERE hookType = :hookType")
    suspend fun countByHookType(hookType: String): Int
}
