package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenRelicPresentationMapperTest {
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
    fun masksHiddenRelicUntilRevealed() {
        val presentation = HiddenRelicPresentationMapper.present(
            relic = hiddenRelic,
            visibility = RelicVisibility.HIDDEN,
        )
        assertTrue(presentation.isMasked)
        assertEquals(HiddenRelicDiscoverySchema.Placeholders.HIDDEN_NAME, presentation.name)
        assertEquals(HiddenRelicDiscoverySchema.Placeholders.HIDDEN_DESCRIPTION, presentation.description)
        assertEquals(0, presentation.piecesCollected)
    }

    @Test
    fun revealsDetailsAfterFirstPiece() {
        val revealTimestamp = 1_700_000_000_000L
        val presentation = HiddenRelicPresentationMapper.present(
            relic = hiddenRelic.copy(piecesCollected = 1),
            visibility = RelicVisibility.HIDDEN,
            revealedAtMs = revealTimestamp,
        )
        assertFalse(presentation.isMasked)
        assertEquals(hiddenRelic.name, presentation.name)
        assertEquals(hiddenRelic.description, presentation.description)
        assertEquals(1, presentation.piecesCollected)
        assertEquals(revealTimestamp, presentation.revealedAtMs)
    }

    @Test
    fun visibleRelics_showFullDetails() {
        val presentation = HiddenRelicPresentationMapper.present(
            relic = hiddenRelic,
            visibility = RelicVisibility.VISIBLE,
        )
        assertFalse(presentation.isMasked)
        assertEquals(hiddenRelic.name, presentation.name)
    }
}
