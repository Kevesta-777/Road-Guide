package com.example.roadguideapp.goldhunt.radar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RadarTypeCooldownStateDao {
    @Query("SELECT * FROM radar_type_cooldown_state")
    suspend fun getAll(): List<RadarTypeCooldownStateEntity>

    @Query("SELECT * FROM radar_type_cooldown_state WHERE radarTypeId = :radarTypeId LIMIT 1")
    suspend fun get(radarTypeId: String): RadarTypeCooldownStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: RadarTypeCooldownStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<RadarTypeCooldownStateEntity>)

    @Query("DELETE FROM radar_type_cooldown_state")
    suspend fun deleteAll()
}
