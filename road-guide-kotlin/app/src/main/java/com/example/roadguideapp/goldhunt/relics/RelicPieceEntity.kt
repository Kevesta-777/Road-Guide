package com.example.roadguideapp.goldhunt.relics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relic_piece")
internal data class RelicPieceEntity(
    @PrimaryKey val pieceId: String,
    val relicId: String,
    val pieceNumber: Int,
    val totalPieces: Int,
    val sourceType: String,
    val sourceKey: String,
    val discovered: Boolean,
    val discoveryDateMs: Long?,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
