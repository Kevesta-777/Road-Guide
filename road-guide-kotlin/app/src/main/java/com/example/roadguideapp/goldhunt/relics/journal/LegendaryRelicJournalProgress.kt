package com.example.roadguideapp.goldhunt.relics.journal

internal data class LegendaryRelicJournalProgress(
    val trackableCount: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val missingCount: Int = 0,
    val lockedCount: Int = 0,
    val hiddenMaskedCount: Int = 0,
    val piecesFound: Int = 0,
    val piecesMissing: Int = 0,
    val totalPieces: Int = 0,
    val hiddenRelicsDiscovered: Int = 0,
    val hiddenRelicsCatalogCount: Int = 0,
    val totalCreditsEarned: Int = 0,
    val totalXpEarned: Long = 0L,
) {
    val completionFraction: Float
        get() = if (trackableCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / trackableCount.toFloat()).coerceIn(0f, 1f)
        }

    val pieceCompletionFraction: Float
        get() = if (totalPieces <= 0) {
            0f
        } else {
            (piecesFound.toFloat() / totalPieces.toFloat()).coerceIn(0f, 1f)
        }

    val piecesMissingFraction: Float
        get() = if (totalPieces <= 0) {
            0f
        } else {
            (piecesMissing.toFloat() / totalPieces.toFloat()).coerceIn(0f, 1f)
        }

    val hiddenDiscoveryFraction: Float
        get() = if (hiddenRelicsCatalogCount <= 0) {
            0f
        } else {
            (hiddenRelicsDiscovered.toFloat() / hiddenRelicsCatalogCount.toFloat()).coerceIn(0f, 1f)
        }
}
