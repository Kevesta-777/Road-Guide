package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition

/**
 * Immutable, indexed view of all registered legendary relic and piece definitions.
 */
internal data class LegendaryRelicRegistrySnapshot(
    val definitions: List<LegendaryRelicDefinition>,
    val byRelicId: Map<String, LegendaryRelicDefinition>,
    val byCategory: Map<RelicCategory, List<LegendaryRelicDefinition>>,
    val pieceDefinitions: List<RelicPieceDefinition>,
    val piecesByRelicId: Map<String, List<RelicPieceDefinition>>,
    val providerContributions: Map<String, Int>,
    val builtAtMs: Long,
) {
    val size: Int get() = definitions.size

    val pieceCount: Int get() = pieceDefinitions.size

    fun findById(relicId: String): LegendaryRelicDefinition? = byRelicId[relicId]

    fun definitionsFor(category: RelicCategory): List<LegendaryRelicDefinition> =
        byCategory[category].orEmpty()

    fun pieceDefinitionsFor(relicId: String): List<RelicPieceDefinition> =
        piecesByRelicId[relicId].orEmpty()

    fun providerIds(): Set<String> = providerContributions.keys
}
