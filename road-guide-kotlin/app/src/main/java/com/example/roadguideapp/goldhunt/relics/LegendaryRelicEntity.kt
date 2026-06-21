package com.example.roadguideapp.goldhunt.relics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legendary_relic")
internal data class LegendaryRelicEntity(
    @PrimaryKey val relicId: String,
    val name: String,
    val description: String,
    val category: String,
    val rarity: String,
    val pieceCount: Int,
    val piecesCollected: Int,
    val completed: Boolean,
    val completionDateMs: Long?,
    val rewardCredits: Int,
    val rewardXp: Long,
    val badgeKey: String?,
    val titleKey: String?,
    val powerKey: String?,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
