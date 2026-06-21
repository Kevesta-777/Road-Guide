package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType

internal data class LegendaryRelicJournalPieceEntry(
    val pieceId: String,
    val pieceNumber: Int,
    val discovered: Boolean,
    val discoveryDateMs: Long?,
    val sourceType: RelicPieceSourceType,
    val masked: Boolean = false,
) {
    val isMissing: Boolean get() = !discovered
}
