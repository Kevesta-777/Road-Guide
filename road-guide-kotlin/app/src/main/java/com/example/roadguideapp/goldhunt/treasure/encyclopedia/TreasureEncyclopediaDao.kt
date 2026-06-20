package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface TreasureEncyclopediaDao {
    @Query("SELECT * FROM treasure_encyclopedia_entry ORDER BY catalogKey ASC")
    suspend fun all(): List<TreasureEncyclopediaEntryEntity>

    @Query("SELECT * FROM treasure_encyclopedia_entry WHERE catalogKey = :catalogKey LIMIT 1")
    suspend fun get(catalogKey: String): TreasureEncyclopediaEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TreasureEncyclopediaEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<TreasureEncyclopediaEntryEntity>)
}
