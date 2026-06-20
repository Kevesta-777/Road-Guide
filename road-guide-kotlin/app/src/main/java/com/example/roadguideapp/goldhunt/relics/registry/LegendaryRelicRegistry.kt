package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicMapper
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition

/**
 * Central registry for all Gold Hunt legendary relic definitions.
 *
 * Providers contribute domain, milestone, and indexed catalog relics; the registry
 * merges them into indexed snapshots suitable for 50+, 100+, and 500+ catalog sizes.
 * Register additional [LegendaryRelicRegistryProvider] instances via [builder] for
 * future content without modifying existing providers.
 */
internal object LegendaryRelicRegistry {
    val defaultProviders: List<LegendaryRelicRegistryProvider> = buildList {
        add(FoundationRelicRegistryProvider)
        RelicCategory.ALL_ORDERED.forEach { category ->
            add(RelicMilestoneRegistryProvider(category = category))
        }
        add(SecretPlaceRelicRegistryProvider)
        add(PanoramaRelicRegistryProvider)
        add(SeasonalEventRelicRegistryProvider)
        add(ClusterRelicRegistryProvider)
        add(RadarRelicRegistryProvider)
        add(TreasureRarityRelicRegistryProvider)
    }

    private val cachedSnapshot: LegendaryRelicRegistrySnapshot by lazy {
        builder().build()
    }

    fun snapshot(): LegendaryRelicRegistrySnapshot = cachedSnapshot

    fun allDefinitions(): List<LegendaryRelicDefinition> = snapshot().definitions

    fun findById(relicId: String): LegendaryRelicDefinition? = snapshot().findById(relicId)

    fun definitionsFor(category: RelicCategory): List<LegendaryRelicDefinition> =
        snapshot().definitionsFor(category)

    fun allPieceDefinitions(): List<RelicPieceDefinition> = snapshot().pieceDefinitions

    fun pieceDefinitionsFor(relicId: String): List<RelicPieceDefinition> =
        snapshot().pieceDefinitionsFor(relicId)

    fun count(): Int = snapshot().size

    fun pieceCount(): Int = snapshot().pieceCount

    fun providerIds(): Set<String> = snapshot().providerIds()

    fun providerContributions(): Map<String, Int> = snapshot().providerContributions

    fun allRelics(): List<LegendaryRelic> =
        allDefinitions().map { definition ->
            LegendaryRelicCatalog.relicFromDefinition(definition)
        }

    fun relicById(relicId: String): LegendaryRelic? =
        findById(relicId)?.let { definition ->
            LegendaryRelicMapper.fromDefinition(definition)
        }

    fun builder(
        extraProviders: List<LegendaryRelicRegistryProvider> = emptyList(),
    ): LegendaryRelicRegistryBuilder =
        LegendaryRelicRegistryBuilder()
            .registerAll(defaultProviders)
            .registerAll(extraProviders)

    fun rebuild(
        extraProviders: List<LegendaryRelicRegistryProvider> = emptyList(),
    ): LegendaryRelicRegistrySnapshot = builder(extraProviders).build()
}
