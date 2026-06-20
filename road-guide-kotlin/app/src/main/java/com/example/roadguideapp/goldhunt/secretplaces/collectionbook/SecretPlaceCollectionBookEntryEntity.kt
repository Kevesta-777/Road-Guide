package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secret_place_collection_book_entry")
internal data class SecretPlaceCollectionBookEntryEntity(
    @PrimaryKey val catalogKey: String,
    val status: String,
    val firstDiscoveredAtMs: Long? = null,
    val completedAtMs: Long? = null,
    val totalCreditsEarned: Int = 0,
    val totalXpEarned: Long = 0L,
    val discoveredCount: Int = 0,
    val completedCount: Int = 0,
    val minimumExplorerLevel: Int = 1,
    val achievementKey: String? = null,
    val storyFragmentId: String? = null,
    val schemaVersion: Int = SecretPlaceCollectionBookSchema.VERSION,
    val extensionJson: String = SecretPlaceCollectionBookSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
