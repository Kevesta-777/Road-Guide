package com.example.roadguideapp.goldhunt.treasure.stats

import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TreasureRarityResolverEventTest {
    private val spec = TreasureSpec(
        treasureId = "tr:event:v1:r:-0.5000:51.4000:summer_explorer:2026:L1:5:6:s0",
        type = TreasureType.STAR,
        lat = 51.45,
        lng = -0.2,
        creditAmount = 2,
        placementKind = "EVENT_SUMMER_EXPLORER",
        regionL1Id = "L1:5:6",
    )

    @Test
    fun resolve_eventTreasureId_isDeterministic() {
        val first = TreasureRarityResolver.resolve(spec)
        val second = TreasureRarityResolver.resolve(spec)

        assertEquals(first, second)
        assertNotNull(TreasureRarityResolver.resolveFromEventTreasureId(spec.treasureId))
    }
}
