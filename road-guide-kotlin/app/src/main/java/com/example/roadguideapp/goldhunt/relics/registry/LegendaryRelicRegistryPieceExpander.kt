package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceDomainInference

internal object LegendaryRelicRegistryPieceExpander {
    fun expand(definitions: List<LegendaryRelicDefinition>): List<RelicPieceDefinition> =
        definitions.flatMap { definition -> expand(definition) }

    fun expand(definition: LegendaryRelicDefinition): List<RelicPieceDefinition> {
        val domainSourceType = RelicPieceSourceDomainInference.sourceTypeForRelicId(definition.relicId)
        if (domainSourceType != null) {
            val sourceKey = RelicPieceSourceDomainInference.sourceKeyForRelicId(
                relicId = definition.relicId,
                sourceType = domainSourceType,
            ) ?: return emptyList()
            return (1..definition.pieceCount).map { pieceNumber ->
                pieceDefinition(
                    definition = definition,
                    pieceNumber = pieceNumber,
                    sourceType = domainSourceType,
                    sourceKey = sourceKey,
                )
            }
        }

        val totalPieces = definition.pieceCount
        return (1..totalPieces).map { pieceNumber ->
            val sourceType = sourceTypeFor(
                category = definition.category,
                pieceNumber = pieceNumber,
                totalPieces = totalPieces,
            )
            pieceDefinition(
                definition = definition,
                pieceNumber = pieceNumber,
                sourceType = sourceType,
                sourceKey = sourceKeyFor(
                    category = definition.category,
                    sourceType = sourceType,
                    pieceNumber = pieceNumber,
                ),
            )
        }
    }

    private fun pieceDefinition(
        definition: LegendaryRelicDefinition,
        pieceNumber: Int,
        sourceType: RelicPieceSourceType,
        sourceKey: String,
    ): RelicPieceDefinition = RelicPieceDefinition(
        pieceId = RelicSchema.PieceKeys.forPiece(definition.relicId, pieceNumber),
        relicId = definition.relicId,
        pieceNumber = pieceNumber,
        totalPieces = definition.pieceCount,
        sourceType = sourceType,
        sourceKey = sourceKey,
    )

    private fun sourceTypeFor(
        category: RelicCategory,
        pieceNumber: Int,
        totalPieces: Int,
    ): RelicPieceSourceType {
        if (totalPieces == 1) {
            return when (category) {
                RelicCategory.STORY -> RelicPieceSourceType.STORY_FRAGMENT
                RelicCategory.EXPLORER -> RelicPieceSourceType.ACHIEVEMENT
                RelicCategory.SEASONAL_EVENT -> RelicPieceSourceType.SEASONAL_EVENT
                else -> RelicPieceSourceType.SECRET_PLACE
            }
        }
        return RelicPieceSourceType.ALL_ORDERED[
            (pieceNumber - 1) % RelicPieceSourceType.ALL_ORDERED.size
        ]
    }

    private fun sourceKeyFor(
        category: RelicCategory,
        sourceType: RelicPieceSourceType,
        pieceNumber: Int,
    ): String = when (sourceType) {
        RelicPieceSourceType.SECRET_PLACE ->
            RelicSchema.SecretPlaceKeys.forCategory(category)
                ?: RelicSchema.AchievementKeys.firstDiscovery(category)
        RelicPieceSourceType.TREASURE_CLUSTER ->
            RelicSchema.PieceSourceKeys.TreasureCluster.forRelicCategory(category, pieceNumber)
        RelicPieceSourceType.ACHIEVEMENT ->
            RelicSchema.AchievementKeys.firstDiscovery(category)
        RelicPieceSourceType.STORY_FRAGMENT ->
            RelicSchema.StoryFragmentKeys.forCategory(category)
        RelicPieceSourceType.PANORAMA_HUNT ->
            RelicSchema.PanoramaHuntKeys.forCategory(category, pieceNumber)
        RelicPieceSourceType.SEASONAL_EVENT ->
            RelicSchema.PieceSourceKeys.SeasonalEvent.forRelicCategory(category, pieceNumber)
        RelicPieceSourceType.RADAR_DISCOVERY ->
            RelicSchema.PieceSourceKeys.RadarDiscovery.forRelicCategory(category, pieceNumber)
    }
}
