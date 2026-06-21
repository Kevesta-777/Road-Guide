package com.example.roadguideapp.goldhunt.relics.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LegendaryRelicRewardGrantDao {
    @Query("SELECT * FROM legendary_relic_reward_grant WHERE relicId = :relicId LIMIT 1")
    suspend fun get(relicId: String): LegendaryRelicRewardGrantEntity?

    @Query("SELECT * FROM legendary_relic_reward_grant ORDER BY grantedAtMs ASC")
    suspend fun getAll(): List<LegendaryRelicRewardGrantEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: LegendaryRelicRewardGrantEntity): Long
}
