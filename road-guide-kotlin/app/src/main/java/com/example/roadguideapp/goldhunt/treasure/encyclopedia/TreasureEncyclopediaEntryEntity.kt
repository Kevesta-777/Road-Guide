package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treasure_encyclopedia_entry")
internal data class TreasureEncyclopediaEntryEntity(
    @PrimaryKey val catalogKey: String,
    val status: String,
    val firstDiscoveredAtMs: Long? = null,
    val bestRarityId: String? = null,
    val totalCreditsEarned: Int = 0,
    val collectionCount: Int = 0,
    val schemaVersion: Int = TreasureEncyclopediaSchema.VERSION,
    val extensionJson: String = TreasureEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
