package com.example.roadguideapp.goldhunt.relics



import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry



/**

 * Read-only facade over [LegendaryRelicRegistry] piece definitions.

 */

internal object RelicPieceCatalog {

    fun allPieceDefinitions(): List<RelicPieceDefinition> =

        LegendaryRelicRegistry.allPieceDefinitions()



    fun foundationPieceDefinitions(): List<RelicPieceDefinition> =

        LegendaryRelicCatalog.foundationDefinitions().flatMap { definition ->

            LegendaryRelicRegistry.pieceDefinitionsFor(definition.relicId)

        }



    fun pieceDefinitionsFor(relicId: String): List<RelicPieceDefinition> =

        LegendaryRelicRegistry.pieceDefinitionsFor(relicId)



    fun pieceDefinitionsFor(definition: LegendaryRelicDefinition): List<RelicPieceDefinition> =

        LegendaryRelicRegistry.pieceDefinitionsFor(definition.relicId)



    fun findById(pieceId: String): RelicPieceDefinition? =

        allPieceDefinitions().firstOrNull { it.pieceId == pieceId }



    fun foundationPieces(): List<RelicPiece> =

        foundationPieceDefinitions().map { definition ->

            RelicPieceMapper.fromDefinition(definition)

        }



    fun pieceFromDefinition(

        definition: RelicPieceDefinition,

        discovered: Boolean = false,

        discoveryDate: Long? = null,

    ): RelicPiece = RelicPieceMapper.fromDefinition(

        definition = definition,

        discovered = discovered,

        discoveryDate = discoveryDate,

    )



    fun discoveredCountFor(relicId: String, pieces: List<RelicPiece>): Int =

        pieces.count { it.relicId == relicId && it.discovered }

}


