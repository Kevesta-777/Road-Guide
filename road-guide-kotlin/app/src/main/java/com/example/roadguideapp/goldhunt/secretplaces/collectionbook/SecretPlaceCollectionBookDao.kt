package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SecretPlaceCollectionBookDao {
    @Query("SELECT * FROM secret_place_collection_book_entry ORDER BY catalogKey ASC")
    suspend fun all(): List<SecretPlaceCollectionBookEntryEntity>

    @Query("SELECT * FROM secret_place_collection_book_entry WHERE catalogKey = :catalogKey LIMIT 1")
    suspend fun get(catalogKey: String): SecretPlaceCollectionBookEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SecretPlaceCollectionBookEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<SecretPlaceCollectionBookEntryEntity>)
}
