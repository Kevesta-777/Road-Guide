package com.example.roadguideapp.goldhunt.relics.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class LegendaryRelicProgressTest {
    @Test
    fun completionPercentages_deriveFromCounts() {
        val progress = LegendaryRelicProgress(
            catalogCount = 10,
            completedCount = 4,
            piecesCollected = 15,
            totalPieces = 30,
            relics = emptyList(),
            pieces = emptyList(),
        )
        assertEquals(0.4f, progress.completionPercentage, 0.0001f)
        assertEquals(0.5f, progress.pieceCompletionPercentage, 0.0001f)
    }
}
