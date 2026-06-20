package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal object FoundationRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "foundation"

    override fun definitions(): List<LegendaryRelicDefinition> =
        RelicCategory.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::foundation)
}

internal class RelicMilestoneRegistryProvider(
    private val category: RelicCategory,
    private val milestones: List<Int> = RelicMilestoneScale.STANDARD,
) : LegendaryRelicRegistryProvider {
    override val providerId: String = "milestone_${category.id.lowercase()}"

    override fun definitions(): List<LegendaryRelicDefinition> =
        milestones.map { milestone ->
            LegendaryRelicRegistryAdapters.milestone(category, milestone)
        }
}

internal class CatalogIndexedRelicRegistryProvider(
    private val category: RelicCategory,
    private val indices: IntRange,
) : LegendaryRelicRegistryProvider {
    override val providerId: String = "catalog_${category.id.lowercase()}"

    override fun definitions(): List<LegendaryRelicDefinition> =
        indices.map { index ->
            LegendaryRelicRegistryAdapters.catalogIndexed(category, index)
        }
}

internal object SecretPlaceRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "secret_place"

    override fun definitions(): List<LegendaryRelicDefinition> =
        SecretPlaceCategory.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromSecretPlace)
}

internal object PanoramaRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "panorama"

    override fun definitions(): List<LegendaryRelicDefinition> =
        PanoramaHuntType.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromPanorama)
}

internal object SeasonalEventRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "seasonal_event"

    override fun definitions(): List<LegendaryRelicDefinition> =
        SeasonalEventType.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromSeasonalEvent)
}

internal object ClusterRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "treasure_cluster"

    override fun definitions(): List<LegendaryRelicDefinition> =
        ClusterType.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromCluster)
}

internal object RadarRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "radar"

    override fun definitions(): List<LegendaryRelicDefinition> =
        RadarType.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromRadar)
}

internal object TreasureRarityRelicRegistryProvider : LegendaryRelicRegistryProvider {
    override val providerId: String = "treasure_rarity"

    override fun definitions(): List<LegendaryRelicDefinition> =
        TreasureRarity.ALL_ORDERED.map(LegendaryRelicRegistryAdapters::fromTreasureRarity)
}
