package com.example.roadguideapp.goldhunt.relics.progress

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicPiece

/**
 * Aggregated legendary relic collection state for UI and profile integration.
 */
internal data class LegendaryRelicProgress(
    val catalogCount: Int,
    val completedCount: Int,
    val piecesCollected: Int,
    val totalPieces: Int,
    val relics: List<LegendaryRelic>,
    val pieces: List<RelicPiece>,
) {
    val completionPercentage: Float
        get() = if (catalogCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / catalogCount.toFloat()).coerceIn(0f, 1f)
        }

    val pieceCompletionPercentage: Float
        get() = if (totalPieces <= 0) {
            0f
        } else {
            (piecesCollected.toFloat() / totalPieces.toFloat()).coerceIn(0f, 1f)
        }

    companion object {
        val EMPTY = LegendaryRelicProgress(
            catalogCount = 0,
            completedCount = 0,
            piecesCollected = 0,
            totalPieces = 0,
            relics = emptyList(),
            pieces = emptyList(),
        )
    }
}
