package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTreasureTypePickerTest {
    @Test
    fun pick_sameSeed_isDeterministic() {
        val profile = EventTreasureProfileCatalog.profileFor(SeasonalEventType.SPRING_BLOSSOM)
        val first = EventTreasureTypePicker.pick("seed-a", profile, TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT)
        val second = EventTreasureTypePicker.pick("seed-a", profile, TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT)

        assertEquals(first, second)
    }

    @Test
    fun normalizedWeights_starOnlyTier_clampsToStar() {
        val profile = EventTreasureProfileCatalog.profileFor(SeasonalEventType.HALLOWEEN_MYSTERY)
        val weights = EventTreasureTypePicker.normalizedWeights(
            profile,
            TreasureGenerationTier.STAR_ONLY,
        )

        assertEquals(listOf(TreasureType.STAR to 1.0), weights)
    }

    @Test
    fun normalizedWeights_springBlossom_favorsFlower() {
        val profile = EventTreasureProfileCatalog.profileFor(SeasonalEventType.SPRING_BLOSSOM)
        val weights = EventTreasureTypePicker.normalizedWeights(
            profile,
            TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT,
        )
        val flowerWeight = weights.first { it.first == TreasureType.FLOWER }.second

        assertTrue(flowerWeight > 0.5)
    }
}
