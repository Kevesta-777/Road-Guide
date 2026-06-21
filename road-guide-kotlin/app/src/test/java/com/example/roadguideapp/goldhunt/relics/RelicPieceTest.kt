package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicPieceTest {
    private val piece = RelicPiece(
        pieceId = RelicSchema.PieceKeys.forPiece(
            RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
            pieceNumber = 2,
        ),
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
        pieceNumber = 2,
        totalPieces = 3,
        sourceType = RelicPieceSourceType.ACHIEVEMENT,
        discovered = false,
        discoveryDate = null,
    )

    @Test
    fun undiscoveredPiece_hasNoDiscoveryDate() {
        assertFalse(piece.discovered)
        assertTrue(piece.isUndiscovered)
        assertNull(piece.discoveryDate)
    }

    @Test
    fun withDiscovery_recordsDiscoveryDate() {
        val discovered = piece.withDiscovery(discovered = true, discoveryDate = 4_000L)
        assertTrue(discovered.discovered)
        assertEquals(4_000L, discovered.discoveryDate)
    }
}
