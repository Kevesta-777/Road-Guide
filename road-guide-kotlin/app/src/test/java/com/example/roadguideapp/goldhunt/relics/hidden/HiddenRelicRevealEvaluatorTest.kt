package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenRelicRevealEvaluatorTest {
    private val hiddenRelic = LegendaryRelic(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
        name = "Veiled Crown",
        description = "A relic concealed from explorers.",
        category = RelicCategory.HIDDEN,
        rarity = RelicRarity.LEGENDARY,
        pieceCount = 5,
        piecesCollected = 0,
        completed = false,
        completionDate = null,
        rewardCredits = 200,
        rewardXp = 500L,
    )

    @Test
    fun visibleRelics_areAlwaysRevealed() {
        assertTrue(
            HiddenRelicRevealEvaluator.isRevealed(
                visibility = RelicVisibility.VISIBLE,
                relic = hiddenRelic,
            ),
        )
    }

    @Test
    fun hiddenRelic_masksUntilFirstPieceOrRevealTimestamp() {
        assertFalse(
            HiddenRelicRevealEvaluator.isRevealed(
                visibility = RelicVisibility.HIDDEN,
                relic = hiddenRelic,
            ),
        )
        assertTrue(
            HiddenRelicRevealEvaluator.isRevealed(
                visibility = RelicVisibility.HIDDEN,
                relic = hiddenRelic.copy(piecesCollected = 1),
            ),
        )
        assertTrue(
            HiddenRelicRevealEvaluator.isRevealed(
                visibility = RelicVisibility.HIDDEN,
                relic = hiddenRelic,
                revealedAtMs = 1_700_000_000_000L,
            ),
        )
    }

    @Test
    fun shouldMaskDetails_matchesRevealState() {
        assertTrue(
            HiddenRelicRevealEvaluator.shouldMaskDetails(
                visibility = RelicVisibility.HIDDEN,
                relic = hiddenRelic,
            ),
        )
        assertFalse(
            HiddenRelicRevealEvaluator.shouldMaskDetails(
                visibility = RelicVisibility.HIDDEN,
                relic = hiddenRelic.copy(piecesCollected = 1),
            ),
        )
    }
}
