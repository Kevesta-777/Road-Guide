package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import org.junit.Assert.assertEquals
import org.junit.Test

class LegendaryRelicJournalStatusResolverTest {
    private val relic = LegendaryRelic(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
        name = "Explorer Relic",
        description = "Description",
        category = RelicCategory.EXPLORER,
        rarity = RelicRarity.COMMON,
        pieceCount = 3,
        piecesCollected = 0,
        completed = false,
        completionDate = null,
        rewardCredits = 50,
        rewardXp = 80L,
    )

    @Test
    fun completedRelic_returnsCompleted() {
        assertEquals(
            LegendaryRelicJournalStatus.COMPLETED,
            LegendaryRelicJournalStatusResolver.resolve(
                relic = relic.copy(piecesCollected = 3, completed = true, completionDate = 1_000L),
                visibility = RelicVisibility.VISIBLE,
                revealed = true,
                explorerLevel = 20,
            ),
        )
    }

    @Test
    fun lockedRelic_returnsLocked() {
        assertEquals(
            LegendaryRelicJournalStatus.LOCKED,
            LegendaryRelicJournalStatusResolver.resolve(
                relic = relic,
                visibility = RelicVisibility.VISIBLE,
                revealed = true,
                explorerLevel = 1,
            ),
        )
    }

    @Test
    fun hiddenUnrevealedRelic_returnsHiddenMasked() {
        assertEquals(
            LegendaryRelicJournalStatus.HIDDEN_MASKED,
            LegendaryRelicJournalStatusResolver.resolve(
                relic = relic,
                visibility = RelicVisibility.HIDDEN,
                revealed = false,
                explorerLevel = 20,
            ),
        )
    }

    @Test
    fun inProgressRelic_returnsInProgress() {
        assertEquals(
            LegendaryRelicJournalStatus.IN_PROGRESS,
            LegendaryRelicJournalStatusResolver.resolve(
                relic = relic.copy(piecesCollected = 1),
                visibility = RelicVisibility.VISIBLE,
                revealed = true,
                explorerLevel = 20,
            ),
        )
    }

    @Test
    fun missingRelic_returnsMissing() {
        assertEquals(
            LegendaryRelicJournalStatus.MISSING,
            LegendaryRelicJournalStatusResolver.resolve(
                relic = relic,
                visibility = RelicVisibility.VISIBLE,
                revealed = true,
                explorerLevel = 20,
            ),
        )
    }
}
