package com.example.roadguideapp.goldhunt.relics.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LegendaryRelicRewardHookDao {
    @Query("SELECT eventId FROM legendary_relic_reward_hook WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Query(
        """
        SELECT eventId FROM legendary_relic_reward_hook
        WHERE hookKey = :hookKey AND hookType = :hookType
        LIMIT 1
        """,
    )
    suspend fun findHook(hookKey: String, hookType: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: LegendaryRelicRewardHookEntity): Long

    @Query("SELECT * FROM legendary_relic_reward_hook ORDER BY grantedAtMs ASC")
    suspend fun getAll(): List<LegendaryRelicRewardHookEntity>

    @Query("SELECT * FROM legendary_relic_reward_hook WHERE hookType = :hookType ORDER BY grantedAtMs ASC")
    suspend fun getAllByHookType(hookType: String): List<LegendaryRelicRewardHookEntity>
}
