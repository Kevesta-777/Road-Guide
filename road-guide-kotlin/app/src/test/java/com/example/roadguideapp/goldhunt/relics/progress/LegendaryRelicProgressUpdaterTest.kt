package com.example.roadguideapp.goldhunt.relics.progress

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicProgressUpdaterTest {
    private val relic = LegendaryRelic(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
        name = "Mythical Relic",
        description = "Test relic",
        category = RelicCategory.MYTHICAL,
        rarity = RelicRarity.EPIC,
        pieceCount = 3,
        piecesCollected = 0,
        completed = false,
        completionDate = null,
        rewardCredits = 100,
        rewardXp = 200L,
    )

    @Test
    fun applyPieceDiscovery_updatesCollectedCount() {
        val updated = LegendaryRelicProgressUpdater.applyPieceDiscovery(
            relic = relic,
            discoveredCount = 2,
            timestampMs = 1_000L,
        )
        assertEquals(2, updated.piecesCollected)
        assertEquals(false, updated.completed)
    }

    @Test
    fun applyPieceDiscovery_completesRelicWhenAllPiecesFound() {
        val updated = LegendaryRelicProgressUpdater.applyPieceDiscovery(
            relic = relic,
            discoveredCount = 3,
            timestampMs = 2_000L,
        )
        assertTrue(updated.completed)
        assertEquals(3, updated.piecesCollected)
        assertEquals(2_000L, updated.completionDate)
    }
}
