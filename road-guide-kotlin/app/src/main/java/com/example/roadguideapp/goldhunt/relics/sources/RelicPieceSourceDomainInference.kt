package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Infers domain-specific relic provenance from registered relic identifiers.
 */
internal object RelicPieceSourceDomainInference {
    fun sourceTypeForRelicId(relicId: String): RelicPieceSourceType? = when {
        relicId.startsWith("${RelicSchema.DomainKeys.SecretPlace.PREFIX}_") ->
            RelicPieceSourceType.SECRET_PLACE
        relicId.startsWith("${RelicSchema.DomainKeys.Cluster.PREFIX}_") ->
            RelicPieceSourceType.TREASURE_CLUSTER
        relicId.startsWith("${RelicSchema.DomainKeys.Panorama.PREFIX}_") ->
            RelicPieceSourceType.PANORAMA_HUNT
        relicId.startsWith("${RelicSchema.DomainKeys.SeasonalEvent.PREFIX}_") ->
            RelicPieceSourceType.SEASONAL_EVENT
        relicId.startsWith("${RelicSchema.DomainKeys.Radar.PREFIX}_") ->
            RelicPieceSourceType.RADAR_DISCOVERY
        else -> null
    }

    fun sourceKeyForRelicId(relicId: String, sourceType: RelicPieceSourceType): String? =
        when (sourceType) {
            RelicPieceSourceType.SECRET_PLACE ->
                secretPlaceCategoryFromRelicId(relicId)?.let {
                    RelicSchema.PieceSourceKeys.SecretPlace.forCategory(it)
                }
            RelicPieceSourceType.TREASURE_CLUSTER ->
                clusterTypeFromRelicId(relicId)?.let {
                    RelicSchema.PieceSourceKeys.TreasureCluster.forType(it)
                }
            RelicPieceSourceType.PANORAMA_HUNT ->
                panoramaTypeFromRelicId(relicId)?.let {
                    RelicSchema.PieceSourceKeys.PanoramaHunt.forType(it)
                }
            RelicPieceSourceType.SEASONAL_EVENT ->
                seasonalEventTypeFromRelicId(relicId)?.let {
                    RelicSchema.PieceSourceKeys.SeasonalEvent.forType(it)
                }
            RelicPieceSourceType.RADAR_DISCOVERY ->
                radarTypeFromRelicId(relicId)?.let {
                    RelicSchema.PieceSourceKeys.RadarDiscovery.forType(it)
                }
            else -> null
        }

    private fun secretPlaceCategoryFromRelicId(relicId: String): SecretPlaceCategory? =
        domainSuffix(relicId, RelicSchema.DomainKeys.SecretPlace.PREFIX)?.let { suffix ->
            SecretPlaceCategory.entries.firstOrNull { it.id.lowercase() == suffix.lowercase() }
        }

    private fun clusterTypeFromRelicId(relicId: String): ClusterType? =
        domainSuffix(relicId, RelicSchema.DomainKeys.Cluster.PREFIX)?.let { suffix ->
            ClusterType.entries.firstOrNull { it.id.lowercase() == suffix.lowercase() }
        }

    private fun panoramaTypeFromRelicId(relicId: String): PanoramaHuntType? =
        domainSuffix(relicId, RelicSchema.DomainKeys.Panorama.PREFIX)?.let { suffix ->
            PanoramaHuntType.entries.firstOrNull { it.id.lowercase() == suffix.lowercase() }
        }

    private fun seasonalEventTypeFromRelicId(relicId: String): SeasonalEventType? =
        domainSuffix(relicId, RelicSchema.DomainKeys.SeasonalEvent.PREFIX)?.let { suffix ->
            SeasonalEventType.entries.firstOrNull { it.id.lowercase() == suffix.lowercase() }
        }

    private fun radarTypeFromRelicId(relicId: String): RadarType? =
        domainSuffix(relicId, RelicSchema.DomainKeys.Radar.PREFIX)?.let { suffix ->
            RadarType.entries.firstOrNull { it.id.lowercase() == suffix.lowercase() }
        }

    private fun domainSuffix(relicId: String, prefix: String): String? {
        val delimiter = "${prefix}_"
        if (!relicId.startsWith(delimiter)) return null
        return relicId.removePrefix(delimiter).takeIf { it.isNotBlank() }
    }
}
