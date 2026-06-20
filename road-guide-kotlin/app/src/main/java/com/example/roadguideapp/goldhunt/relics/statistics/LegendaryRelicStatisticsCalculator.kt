package com.example.roadguideapp.goldhunt.relics.statistics

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity

internal object LegendaryRelicStatisticsCalculator {
    fun compute(
        relics: List<LegendaryRelic>,
        grants: List<LegendaryRelicRewardGrantEntity>,
        catalogSize: Int,
        totalPieces: Int,
        hiddenRelicsDiscovered: Int,
        hiddenRelicsCatalogCount: Int,
    ): LegendaryRelicStatistics {
        val completedRelics = relics.filter { it.completed }
        return LegendaryRelicStatistics(
            catalogCount = catalogSize.coerceAtLeast(0),
            completedCount = completedRelics.size,
            piecesCollected = relics.sumOf { it.piecesCollected.coerceAtLeast(0) },
            totalPieces = totalPieces.coerceAtLeast(0),
            hiddenRelicsDiscovered = hiddenRelicsDiscovered.coerceAtLeast(0),
            hiddenRelicsCatalogCount = hiddenRelicsCatalogCount.coerceAtLeast(0),
            epicCompletedCount = completedRelics.count { it.rarity == RelicRarity.EPIC },
            legendaryCompletedCount = completedRelics.count { it.rarity == RelicRarity.LEGENDARY },
            creditsEarned = grants.sumOf { it.creditsGranted.coerceAtLeast(0) },
            xpEarned = grants.sumOf { it.xpGranted.coerceAtLeast(0L) },
        )
    }
}
