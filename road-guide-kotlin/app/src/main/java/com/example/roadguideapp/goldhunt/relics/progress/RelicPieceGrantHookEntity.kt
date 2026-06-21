package com.example.roadguideapp.goldhunt.relics.progress

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceSchema

@Entity(tableName = "relic_piece_grant_hook")
internal data class RelicPieceGrantHookEntity(
    @PrimaryKey val eventId: String,
    val pieceId: String,
    val relicId: String,
    val hookType: String = RelicPieceSourceSchema.HookTypes.RELIC_PIECE,
    val hookKey: String,
    val grantedAtMs: Long,
    val schemaVersion: Int = RelicPieceSourceSchema.VERSION,
)
