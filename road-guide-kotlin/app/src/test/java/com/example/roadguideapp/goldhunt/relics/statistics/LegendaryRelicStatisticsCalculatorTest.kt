package com.example.roadguideapp.goldhunt.relics.statistics

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LegendaryRelicStatisticsCalculatorTest {
    @Test
    fun compute_tracksPiecesCollectedCompletionPercentageRewardsAndHiddenDiscovery() {
        val relics = listOf(
            relic(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
                rarity = RelicRarity.COMMON,
                pieceCount = 1,
                piecesCollected = 1,
                completed = true,
            ),
            relic(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
                rarity = RelicRarity.EPIC,
                pieceCount = 3,
                piecesCollected = 2,
                completed = false,
            ),
            relic(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
                rarity = RelicRarity.LEGENDARY,
                pieceCount = 5,
                piecesCollected = 5,
                completed = true,
            ),
        )
        val grants = listOf(
            LegendaryRelicRewardGrantEntity(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
                creditsGranted = 50,
                xpGranted = 80L,
                storyFragmentKey = null,
                storyFragmentRecorded = false,
                titleKey = null,
                titleRecorded = false,
                badgeKey = null,
                badgeRecorded = false,
                cosmeticKey = null,
                cosmeticRecorded = false,
                grantedAtMs = 1_000L,
            ),
            LegendaryRelicRewardGrantEntity(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
                creditsGranted = 250,
                xpGranted = 500L,
                storyFragmentKey = null,
                storyFragmentRecorded = false,
                titleKey = null,
                titleRecorded = false,
                badgeKey = null,
                badgeRecorded = false,
                cosmeticKey = null,
                cosmeticRecorded = false,
                grantedAtMs = 2_000L,
            ),
        )

        val stats = LegendaryRelicStatisticsCalculator.compute(
            relics = relics,
            grants = grants,
            catalogSize = 3,
            totalPieces = 9,
            hiddenRelicsDiscovered = 2,
            hiddenRelicsCatalogCount = 5,
        )

        assertEquals(3, stats.catalogCount)
        assertEquals(2, stats.completedCount)
        assertEquals(8, stats.piecesCollected)
        assertEquals(9, stats.totalPieces)
        assertEquals(2, stats.hiddenRelicsDiscovered)
        assertEquals(5, stats.hiddenRelicsCatalogCount)
        assertEquals(3, stats.hiddenRelicsUndiscovered)
        assertEquals(0, stats.epicCompletedCount)
        assertEquals(1, stats.legendaryCompletedCount)
        assertEquals(300, stats.creditsEarned)
        assertEquals(580L, stats.xpEarned)
        assertEquals(2f / 3f, stats.completionPercentage, 0.0001f)
        assertEquals(8f / 9f, stats.pieceCompletionPercentage, 0.0001f)
        assertEquals(2f / 5f, stats.hiddenDiscoveryPercentage, 0.0001f)
    }

    @Test
    fun withGrant_incrementsCompletionAndRewardTotals() {
        val relic = relic(
            relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
            rarity = RelicRarity.LEGENDARY,
            pieceCount = 5,
            piecesCollected = 5,
            completed = true,
        )
        val updated = LegendaryRelicStatistics.EMPTY.withGrant(
            relic = relic,
            grantResult = com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantResult(
                relicId = relic.relicId,
                creditsGranted = 120,
                xpGranted = 240L,
                isNewGrant = true,
            ),
        )
        assertEquals(1, updated.completedCount)
        assertEquals(1, updated.legendaryCompletedCount)
        assertEquals(120, updated.creditsEarned)
        assertEquals(240L, updated.xpEarned)
    }

    private fun relic(
        relicId: String,
        rarity: RelicRarity,
        pieceCount: Int,
        piecesCollected: Int,
        completed: Boolean,
    ): LegendaryRelic = LegendaryRelic(
        relicId = relicId,
        name = "Relic",
        description = "Description",
        category = RelicCategory.MYTHICAL,
        rarity = rarity,
        pieceCount = pieceCount,
        piecesCollected = piecesCollected,
        completed = completed,
        completionDate = if (completed) 1_000L else null,
        rewardCredits = 999,
        rewardXp = 999L,
    )
}
