package com.example.roadguideapp.goldhunt.events.achievements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SeasonalEventAchievementCompletionDao {
    @Query("SELECT eventId FROM seasonal_event_achievement_completion WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SeasonalEventAchievementCompletionEntity): Long

    @Query("SELECT * FROM seasonal_event_achievement_completion ORDER BY completedAtMs DESC")
    suspend fun loadAll(): List<SeasonalEventAchievementCompletionEntity>
}
