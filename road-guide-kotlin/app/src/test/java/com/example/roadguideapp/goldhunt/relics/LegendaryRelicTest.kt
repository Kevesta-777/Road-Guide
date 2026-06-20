package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicTest {
    private val relic = LegendaryRelic(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
        name = "Mythical Relic",
        description = "A relic from myth.",
        category = RelicCategory.MYTHICAL,
        rarity = RelicRarity.EPIC,
        pieceCount = 5,
        piecesCollected = 2,
        completed = false,
        completionDate = null,
        rewardCredits = 120,
        rewardXp = 280L,
        badgeKey = "badge_relic:legendary_relic_mythical",
        titleKey = "title_relic:legendary_relic_mythical",
        powerKey = "relic_power:legendary_relic_mythical",
    )

    @Test
    fun progressFraction_scalesWithPiecesCollected() {
        assertEquals(0.4f, relic.progressFraction, 0.0001f)
    }

    @Test
    fun withProgress_completesWhenAllPiecesCollected() {
        val completed = relic.withProgress(piecesCollected = 5, completionDate = 3_000L)
        assertTrue(completed.completed)
        assertEquals(5, completed.piecesCollected)
        assertEquals(3_000L, completed.completionDate)
        assertEquals(1f, completed.progressFraction, 0.0001f)
    }

    @Test
    fun futureRewardFlags_detectConfiguredHooks() {
        assertTrue(relic.hasBadgeReward)
        assertTrue(relic.hasTitleReward)
        assertTrue(relic.hasPowerReward)
    }

    @Test
    fun incompleteRelic_hasNoCompletionDate() {
        assertFalse(relic.completed)
        assertNull(relic.completionDate)
    }
}
