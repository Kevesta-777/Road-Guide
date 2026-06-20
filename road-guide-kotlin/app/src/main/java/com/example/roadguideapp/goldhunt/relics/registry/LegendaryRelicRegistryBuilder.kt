package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition

internal class LegendaryRelicRegistryBuilder {
    private val providers = linkedSetOf<LegendaryRelicRegistryProvider>()

    fun register(provider: LegendaryRelicRegistryProvider): LegendaryRelicRegistryBuilder {
        providers.add(provider)
        return this
    }

    fun registerAll(providers: Collection<LegendaryRelicRegistryProvider>): LegendaryRelicRegistryBuilder {
        providers.forEach { register(it) }
        return this
    }

    fun build(): LegendaryRelicRegistrySnapshot {
        val merged = linkedMapOf<String, LegendaryRelicDefinition>()
        val contributions = linkedMapOf<String, Int>()
        val keyOwners = linkedMapOf<String, String>()
        val duplicateKeys = mutableListOf<String>()

        for (provider in providers) {
            var contributed = 0
            for (definition in provider.definitions()) {
                val previousOwner = keyOwners.put(definition.relicId, provider.providerId)
                if (previousOwner != null) {
                    duplicateKeys += "${definition.relicId} ($previousOwner, ${provider.providerId})"
                    continue
                }
                merged[definition.relicId] = definition
                contributed++
            }
            contributions[provider.providerId] = contributed
        }

        if (duplicateKeys.isNotEmpty()) {
            error(
                "LegendaryRelicRegistry duplicate relicIds detected: ${duplicateKeys.joinToString("; ")}",
            )
        }

        val definitions = merged.values.toList()
        val byCategory = definitions.groupBy { it.category }
            .mapValues { (_, values) -> values.sortedBy { it.relicId } }
            .toSortedMap(compareBy { categoryOrder(it) })

        val pieceDefinitions = LegendaryRelicRegistryPieceExpander.expand(definitions)
        val piecesByRelicId = pieceDefinitions.groupBy { it.relicId }

        return LegendaryRelicRegistrySnapshot(
            definitions = definitions,
            byRelicId = merged,
            byCategory = byCategory,
            pieceDefinitions = pieceDefinitions,
            piecesByRelicId = piecesByRelicId,
            providerContributions = contributions,
            builtAtMs = System.currentTimeMillis(),
        )
    }

    private fun categoryOrder(category: RelicCategory): Int =
        RelicCategory.ALL_ORDERED.indexOf(category).coerceAtLeast(0)
}
