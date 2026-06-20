package com.example.roadguideapp.goldhunt.radar.rewards

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RadarRewardStatisticsDao {
    @Query("SELECT * FROM radar_reward_statistics WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = RadarRewardSchema.STATISTICS_SINGLETON_ID): RadarRewardStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RadarRewardStatisticsEntity)
}
