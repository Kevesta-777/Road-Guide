package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistrySnapshot

/**
 * Indexed lookup of registered relic pieces by provenance key.
 */
internal class RelicPieceSourceIndex private constructor(
    private val bySourceKey: Map<SourceLookupKey, List<RelicPieceDefinition>>,
    val pieceDefinitions: List<RelicPieceDefinition>,
) {
    fun lookup(
        sourceType: RelicPieceSourceType,
        sourceKey: String,
    ): List<RelicPieceDefinition> =
        bySourceKey[SourceLookupKey(sourceType, sourceKey)].orEmpty()

    fun size(): Int = pieceDefinitions.size

    companion object {
        fun fromSnapshot(snapshot: LegendaryRelicRegistrySnapshot): RelicPieceSourceIndex =
            fromDefinitions(snapshot.pieceDefinitions)

        fun fromRegistry(): RelicPieceSourceIndex =
            fromSnapshot(LegendaryRelicRegistry.snapshot())

        fun fromDefinitions(
            pieceDefinitions: List<RelicPieceDefinition>,
        ): RelicPieceSourceIndex {
            val grouped = pieceDefinitions.groupBy { definition ->
                SourceLookupKey(definition.sourceType, definition.sourceKey)
            }
            return RelicPieceSourceIndex(
                bySourceKey = grouped.mapValues { (_, definitions) ->
                    definitions.sortedWith(
                        compareBy<RelicPieceDefinition> { it.relicId }
                            .thenBy { it.pieceNumber },
                    )
                },
                pieceDefinitions = pieceDefinitions,
            )
        }
    }

    private data class SourceLookupKey(
        val sourceType: RelicPieceSourceType,
        val sourceKey: String,
    )
}
