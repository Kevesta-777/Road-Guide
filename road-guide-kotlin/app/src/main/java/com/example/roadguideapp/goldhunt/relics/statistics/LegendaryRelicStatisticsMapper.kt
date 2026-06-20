package com.example.roadguideapp.goldhunt.relics.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry

internal object LegendaryRelicStatisticsMapper {
    fun fromEntity(entity: ExplorerProfileEntity): LegendaryRelicStatistics =
        LegendaryRelicStatistics(
            catalogCount = entity.legendaryRelicStatsCatalogCount.coerceAtLeast(0),
            completedCount = entity.legendaryRelicStatsCompletedCount.coerceAtLeast(0),
            piecesCollected = entity.legendaryRelicStatsPiecesCollected.coerceAtLeast(0),
            totalPieces = entity.legendaryRelicStatsTotalPieces.coerceAtLeast(0),
            hiddenRelicsDiscovered = entity.legendaryRelicStatsHiddenRevealed.coerceAtLeast(0),
            hiddenRelicsCatalogCount = entity.legendaryRelicStatsHiddenCatalogCount.coerceAtLeast(0),
            epicCompletedCount = entity.legendaryRelicStatsEpicCompleted.coerceAtLeast(0),
            legendaryCompletedCount = entity.legendaryRelicStatsLegendaryCompleted.coerceAtLeast(0),
            creditsEarned = entity.legendaryRelicStatsCreditsEarned.coerceAtLeast(0),
            xpEarned = entity.legendaryRelicStatsXpEarned.coerceAtLeast(0L),
        ).withCatalogSize(
            catalogSize = LegendaryRelicRegistry.count().coerceAtLeast(
                entity.legendaryRelicStatsCatalogCount,
            ),
            totalPieces = LegendaryRelicRegistry.pieceCount().coerceAtLeast(
                entity.legendaryRelicStatsTotalPieces,
            ),
        )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: LegendaryRelicStatistics,
    ): ExplorerProfileEntity = entity.copy(
        legendaryRelicStatsCatalogCount = stats.catalogCount.coerceAtLeast(0),
        legendaryRelicStatsCompletedCount = stats.completedCount.coerceAtLeast(0),
        legendaryRelicStatsPiecesCollected = stats.piecesCollected.coerceAtLeast(0),
        legendaryRelicStatsTotalPieces = stats.totalPieces.coerceAtLeast(0),
        legendaryRelicStatsHiddenCatalogCount = stats.hiddenRelicsCatalogCount.coerceAtLeast(0),
        legendaryRelicStatsHiddenRevealed = stats.hiddenRelicsDiscovered.coerceAtLeast(0),
        legendaryRelicStatsHiddenUndiscovered = stats.hiddenRelicsUndiscovered.coerceAtLeast(0),
        legendaryRelicStatsEpicCompleted = stats.epicCompletedCount.coerceAtLeast(0),
        legendaryRelicStatsLegendaryCompleted = stats.legendaryCompletedCount.coerceAtLeast(0),
        legendaryRelicStatsCreditsEarned = stats.creditsEarned.coerceAtLeast(0),
        legendaryRelicStatsXpEarned = stats.xpEarned.coerceAtLeast(0L),
    )
}
