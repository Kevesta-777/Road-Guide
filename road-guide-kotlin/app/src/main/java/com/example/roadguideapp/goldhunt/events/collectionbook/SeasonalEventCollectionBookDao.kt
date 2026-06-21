package com.example.roadguideapp.goldhunt.events.collectionbook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SeasonalEventCollectionBookDao {
    @Query("SELECT * FROM seasonal_event_collection_book_entry ORDER BY firstParticipatedAtMs DESC")
    suspend fun allOrdered(): List<SeasonalEventCollectionBookEntryEntity>

    @Query("SELECT * FROM seasonal_event_collection_book_entry WHERE eventId = :eventId LIMIT 1")
    suspend fun get(eventId: String): SeasonalEventCollectionBookEntryEntity?

    @Query(
        """
        SELECT COUNT(*) FROM seasonal_event_collection_book_entry
        WHERE eventType = :eventType AND status != 'missing'
        """,
    )
    suspend fun countParticipatedForType(eventType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SeasonalEventCollectionBookEntryEntity)
}
