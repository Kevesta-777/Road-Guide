package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicPieceCatalogTest {
    @Test
    fun foundationPieceDefinitions_matchRelicPieceCounts() {
        LegendaryRelicCatalog.foundationDefinitions().forEach { relicDefinition ->
            val pieces = RelicPieceCatalog.pieceDefinitionsFor(relicDefinition)
            assertEquals(relicDefinition.pieceCount, pieces.size)
            pieces.forEach { piece ->
                assertEquals(relicDefinition.relicId, piece.relicId)
                assertTrue(piece.pieceNumber in 1..piece.totalPieces)
                assertEquals(relicDefinition.pieceCount, piece.totalPieces)
            }
        }
    }

    @Test
    fun pieceDefinitions_assignSupportedSourceTypes() {
        val sourceTypes = RelicPieceCatalog.allPieceDefinitions()
            .map { it.sourceType }
            .toSet()
        RelicPieceSourceType.ALL_ORDERED.forEach { sourceType ->
            assertTrue("$sourceType missing from catalog", sourceType in sourceTypes)
        }
    }

    @Test
    fun storyRelic_usesStoryFragmentSource() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.STORY)
        val piece = RelicPieceCatalog.pieceDefinitionsFor(relicId).single()
        assertEquals(RelicPieceSourceType.STORY_FRAGMENT, piece.sourceType)
        assertEquals(
            RelicSchema.StoryFragmentKeys.forCategory(RelicCategory.STORY),
            piece.sourceKey,
        )
    }

    @Test
    fun warriorRelic_assignsRotatingSourceKeys() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR)
        val pieces = RelicPieceCatalog.pieceDefinitionsFor(relicId)
        assertEquals(3, pieces.size)
        assertEquals(RelicPieceSourceType.SECRET_PLACE, pieces[0].sourceType)
        assertEquals(RelicPieceSourceType.TREASURE_CLUSTER, pieces[1].sourceType)
        assertEquals(RelicPieceSourceType.STORY_FRAGMENT, pieces[2].sourceType)
    }

    @Test
    fun discoveredCountFor_countsOnlyMatchingRelicPieces() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL)
        val pieces = RelicPieceCatalog.pieceDefinitionsFor(relicId).mapIndexed { index, definition ->
            RelicPieceCatalog.pieceFromDefinition(
                definition = definition,
                discovered = index < 2,
                discoveryDate = if (index < 2) 1_000L + index else null,
            )
        }
        assertEquals(2, RelicPieceCatalog.discoveredCountFor(relicId, pieces))
    }

    @Test
    fun findById_resolvesFoundationPiece() {
        val definition = RelicPieceCatalog.foundationPieceDefinitions().first()
        assertNotNull(RelicPieceCatalog.findById(definition.pieceId))
    }
}
