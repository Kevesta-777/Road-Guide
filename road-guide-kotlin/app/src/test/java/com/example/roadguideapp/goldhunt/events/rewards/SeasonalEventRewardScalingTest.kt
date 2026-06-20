package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEventFactory
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventRewardScalingTest {
    private val spec = TreasureSpec(
        treasureId = "tr:event:v1:r:-0.5000:51.4000:spring_blossom:2026:L1:1:2:s0",
        type = TreasureType.FLOWER,
        lat = 51.45,
        lng = -0.2,
        creditAmount = 10,
        placementKind = "EVENT_SPRING_BLOSSOM",
        regionL1Id = "L1:1:2",
    )

    private val event = SeasonalEventFactory.fromType(
        SeasonalEventType.SPRING_BLOSSOM,
        LocalDate.of(2026, 4, 10),
    )

    @Test
    fun combinedMultiplier_appliesEventLevelAndRarity() {
        val context = SeasonalEventRewardContext(
            spec = spec,
            event = event,
            explorerLevel = 5,
            rarity = TreasureRarity.RARE,
        )
        val breakdown = SeasonalEventRewardScaling.breakdown(context)

        assertEquals(1.15, breakdown.eventTypeFactor, 0.0001)
        assertEquals(1.12, breakdown.explorerLevelFactor, 0.0001)
        assertEquals(3.0, breakdown.rarityFactor, 0.0001)
        assertEquals(1.15 * 1.12 * 3.0, breakdown.combinedMultiplier, 0.0001)
    }

    @Test
    fun scaledCredits_neverBelowBaseAmount() {
        val context = SeasonalEventRewardContext(
            spec = spec,
            event = event,
            explorerLevel = 1,
            rarity = TreasureRarity.COMMON,
        )

        assertTrue(SeasonalEventRewardScaling.scaledCredits(context) >= spec.creditAmount)
    }

    @Test
    fun legendaryRelicProgressIncrement_scalesWithRarityTier() {
        assertEquals(0, SeasonalEventRewardScaling.legendaryRelicProgressIncrement(TreasureRarity.RARE))
        assertEquals(1, SeasonalEventRewardScaling.legendaryRelicProgressIncrement(TreasureRarity.EPIC))
        assertEquals(2, SeasonalEventRewardScaling.legendaryRelicProgressIncrement(TreasureRarity.LEGENDARY))
        assertEquals(3, SeasonalEventRewardScaling.legendaryRelicProgressIncrement(TreasureRarity.MYTHIC))
    }
}
