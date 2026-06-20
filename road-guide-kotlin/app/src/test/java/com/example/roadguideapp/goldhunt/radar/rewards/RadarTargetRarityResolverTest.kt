package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarSignalFactory
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarTargetRarityResolverTest {
    @Test
    fun resolve_isDeterministicForSameTarget() {
        val signal = requireNotNull(
            RadarSignalFactory.secretPlace(
                secretPlaceId = "secret-rarity-test",
                distanceMeters = 120.0,
                detectionRangeMeters = 1_200,
                detectedTime = 1_700_000_000_000L,
            ),
        )

        val first = RadarTargetRarityResolver.resolve(signal)
        val second = RadarTargetRarityResolver.resolve(signal)

        assertEquals(first, second)
        assertTrue(first.tier in 3..4)
    }

    @Test
    fun resolve_legendaryRelicTargetsMythicBand() {
        val signal = requireNotNull(
            RadarSignalFactory.legendaryRelic(
                legendaryRelicKey = "legendary-relic-rarity-test",
                distanceMeters = 50.0,
                detectionRangeMeters = 3_000,
                detectedTime = 1_700_000_000_000L,
            ),
        )

        val rarity = RadarTargetRarityResolver.resolve(signal)

        assertEquals(RadarTargetCategory.LEGENDARY_RELIC, signal.targetCategory)
        assertTrue(rarity.tier >= RadarTargetRarity.LEGENDARY.tier)
    }
}
