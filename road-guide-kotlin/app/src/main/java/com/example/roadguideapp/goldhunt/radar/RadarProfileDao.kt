package com.example.roadguideapp.goldhunt.radar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RadarProfileDao {
    @Query("SELECT * FROM radar_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = RadarSchema.SINGLETON_ID): RadarProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: RadarProfileEntity)

    @Query("DELETE FROM radar_profile WHERE id = :id")
    suspend fun delete(id: Int = RadarSchema.SINGLETON_ID)
}
