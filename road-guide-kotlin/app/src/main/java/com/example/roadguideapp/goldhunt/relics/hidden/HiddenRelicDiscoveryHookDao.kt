package com.example.roadguideapp.goldhunt.relics.hidden

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface HiddenRelicDiscoveryHookDao {
    @Query("SELECT eventId FROM hidden_relic_discovery_hook WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: HiddenRelicDiscoveryHookEntity): Long

    @Query("SELECT * FROM hidden_relic_discovery_hook ORDER BY grantedAtMs ASC")
    suspend fun getAll(): List<HiddenRelicDiscoveryHookEntity>
}
