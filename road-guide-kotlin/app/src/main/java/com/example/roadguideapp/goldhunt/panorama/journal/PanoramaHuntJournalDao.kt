package com.example.roadguideapp.goldhunt.panorama.journal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface PanoramaHuntJournalDao {
    @Query("SELECT * FROM panorama_hunt_journal_entry WHERE catalogKey = :catalogKey LIMIT 1")
    suspend fun get(catalogKey: String): PanoramaHuntJournalEntryEntity?

    @Query("SELECT * FROM panorama_hunt_journal_entry ORDER BY catalogKey ASC")
    suspend fun all(): List<PanoramaHuntJournalEntryEntity>

    @Upsert
    suspend fun upsert(row: PanoramaHuntJournalEntryEntity)

    @Upsert
    suspend fun upsertAll(rows: List<PanoramaHuntJournalEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rows: List<PanoramaHuntJournalEntryEntity>): List<Long>
}
