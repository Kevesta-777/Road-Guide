package com.example.roadguideapp.goldhunt.relics.statistics

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRevealResult
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantResult

/**
 * Lifetime legendary relic statistics integrated with the explorer profile.
 */
internal data class LegendaryRelicStatistics(
    val catalogCount: Int = 0,
    val completedCount: Int = 0,
    val piecesCollected: Int = 0,
    val totalPieces: Int = 0,
    val hiddenRelicsDiscovered: Int = 0,
    val hiddenRelicsCatalogCount: Int = 0,
    val epicCompletedCount: Int = 0,
    val legendaryCompletedCount: Int = 0,
    val creditsEarned: Int = 0,
    val xpEarned: Long = 0L,
) {
    val completionPercentage: Float
        get() = if (catalogCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / catalogCount.toFloat()).coerceIn(0f, 1f)
        }

    val pieceCompletionPercentage: Float
        get() = if (totalPieces <= 0) {
            0f
        } else {
            (piecesCollected.toFloat() / totalPieces.toFloat()).coerceIn(0f, 1f)
        }

    val hiddenRelicsUndiscovered: Int
        get() = (hiddenRelicsCatalogCount - hiddenRelicsDiscovered).coerceAtLeast(0)

    val hiddenDiscoveryPercentage: Float
        get() = if (hiddenRelicsCatalogCount <= 0) {
            0f
        } else {
            (hiddenRelicsDiscovered.toFloat() / hiddenRelicsCatalogCount.toFloat()).coerceIn(0f, 1f)
        }

    fun withCatalogSize(catalogSize: Int, totalPieces: Int): LegendaryRelicStatistics =
        copy(
            catalogCount = catalogSize.coerceAtLeast(0),
            totalPieces = totalPieces.coerceAtLeast(0),
        )

    fun withGrant(
        relic: LegendaryRelic,
        grantResult: LegendaryRelicRewardGrantResult,
    ): LegendaryRelicStatistics {
        if (!grantResult.isNewGrant) return this
        return copy(
            completedCount = completedCount + 1,
            epicCompletedCount = epicCompletedCount +
                if (relic.rarity == RelicRarity.EPIC) 1 else 0,
            legendaryCompletedCount = legendaryCompletedCount +
                if (relic.rarity == RelicRarity.LEGENDARY) 1 else 0,
            creditsEarned = creditsEarned + grantResult.creditsGranted.coerceAtLeast(0),
            xpEarned = xpEarned + grantResult.xpGranted.coerceAtLeast(0L),
        )
    }

    fun withHiddenReveal(
        revealResult: HiddenRelicDiscoveryRevealResult,
    ): LegendaryRelicStatistics {
        if (!revealResult.isNewReveal) return this
        return copy(hiddenRelicsDiscovered = hiddenRelicsDiscovered + 1)
    }

    fun toHiddenDiscoveryStatistics(): HiddenRelicDiscoveryStatistics =
        HiddenRelicDiscoveryStatistics(
            hiddenCatalogCount = hiddenRelicsCatalogCount,
            hiddenRevealedCount = hiddenRelicsDiscovered,
            hiddenUndiscoveredCount = hiddenRelicsUndiscovered,
        )

    companion object {
        val EMPTY = LegendaryRelicStatistics()
    }
}
