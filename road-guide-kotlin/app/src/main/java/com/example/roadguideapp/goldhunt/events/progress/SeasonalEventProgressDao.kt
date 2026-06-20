package com.example.roadguideapp.goldhunt.events.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SeasonalEventProgressDao {
    @Query("SELECT * FROM seasonal_event_progress WHERE eventId = :eventId LIMIT 1")
    suspend fun getByEventId(eventId: String): SeasonalEventProgressEntity?

    @Query("SELECT * FROM seasonal_event_progress ORDER BY updatedAtMs DESC")
    suspend fun getAll(): List<SeasonalEventProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SeasonalEventProgressEntity)
}
