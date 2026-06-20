package com.example.roadguideapp.goldhunt.profile.levelup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LevelUpEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: LevelUpEventEntity): Long

    @Query("SELECT * FROM level_up_event WHERE id = :id LIMIT 1")
    suspend fun get(id: String): LevelUpEventEntity?

    @Query("SELECT * FROM level_up_event ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<LevelUpEventEntity>

    @Query("SELECT COUNT(*) FROM level_up_event")
    suspend fun count(): Int
}
