package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicPieceMapperTest {
    @Test
    fun fromDefinition_buildsUndiscoveredPiece() {
        val definition = RelicPieceCatalog.foundationPieceDefinitions().first()
        val piece = RelicPieceMapper.fromDefinition(definition)

        assertEquals(definition.pieceId, piece.pieceId)
        assertEquals(definition.relicId, piece.relicId)
        assertEquals(definition.pieceNumber, piece.pieceNumber)
        assertEquals(definition.totalPieces, piece.totalPieces)
        assertEquals(definition.sourceType, piece.sourceType)
        assertFalse(piece.discovered)
        assertNull(piece.discoveryDate)
    }

    @Test
    fun fromDefinition_discoveredPiece_recordsDiscoveryDate() {
        val definition = RelicPieceCatalog.foundationPieceDefinitions().first()
        val piece = RelicPieceMapper.fromDefinition(
            definition = definition,
            discovered = true,
            discoveryDate = 6_000L,
        )
        assertTrue(piece.discovered)
        assertEquals(6_000L, piece.discoveryDate)
    }

    @Test
    fun entityRoundTrip_preservesFields() {
        val definition = RelicPieceCatalog.foundationPieceDefinitions().first()
        val original = RelicPieceMapper.fromDefinition(
            definition = definition,
            discovered = true,
            discoveryDate = 7_000L,
        )
        val entity = RelicPieceMapper.toEntity(
            piece = original,
            sourceKey = definition.sourceKey,
            updatedAtMs = 7_500L,
        )
        val restored = RelicPieceMapper.toDomain(entity)

        assertNotNull(restored)
        assertEquals(original.pieceId, restored!!.pieceId)
        assertEquals(original.relicId, restored.relicId)
        assertEquals(original.pieceNumber, restored.pieceNumber)
        assertEquals(original.totalPieces, restored.totalPieces)
        assertEquals(original.sourceType, restored.sourceType)
        assertEquals(original.discovered, restored.discovered)
        assertEquals(original.discoveryDate, restored.discoveryDate)
        assertEquals(definition.sourceKey, entity.sourceKey)
    }

    @Test
    fun toDomain_returnsNullForUnknownSourceType() {
        val definition = RelicPieceCatalog.foundationPieceDefinitions().first()
        val entity = RelicPieceMapper.toEntity(
            piece = RelicPieceMapper.fromDefinition(definition),
            sourceKey = definition.sourceKey,
        ).copy(sourceType = "unknown_source")
        assertNull(RelicPieceMapper.toDomain(entity))
    }
}
