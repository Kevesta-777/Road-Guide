package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.time.LocalDate

class EventTreasureGeneratorTest {
    private val region = PlayRegion(
        west = -0.5,
        south = 51.4,
        east = 0.1,
        north = 51.6,
        totalCellsL0 = 1_000_000L,
    )

    private val bounds = LatLngBounds.Builder()
        .include(LatLng(51.45, -0.2))
        .include(LatLng(51.55, -0.1))
        .build()

    @Test
    fun treasuresInBounds_inactiveDate_returnsEmpty() {
        val treasures = EventTreasureGenerator.treasuresInBounds(
            bounds = bounds,
            region = region,
            isCollected = { false },
            evaluatedAt = LocalDate.of(2026, 2, 10),
        )

        assertTrue(treasures.isEmpty())
    }

    @Test
    fun treasuresInBounds_sameInputs_producesIdenticalOutput() {
        val date = LocalDate.of(2026, 4, 10)
        val first = generate(date)
        val second = generate(date)

        assertEquals(first, second)
    }

    @Test
    fun treasuresInBounds_springBlossom_usesEventPlacementKind() {
        val treasures = generate(LocalDate.of(2026, 4, 10))

        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "EVENT_SPRING_BLOSSOM" })
        assertTrue(treasures.all { it.treasureId.contains(":spring_blossom:") })
    }

    @Test
    fun treasuresInBounds_halloweenMystery_prefersCrystalAndGiftAtFullTier() {
        val treasures = generate(
            date = LocalDate.of(2026, 10, 20),
            tier = TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT,
        )

        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "EVENT_HALLOWEEN_MYSTERY" })
        val types = treasures.map { it.type }.toSet()
        assertTrue(types.all { it == TreasureType.CRYSTAL || it == TreasureType.GIFT || it == TreasureType.FLOWER || it == TreasureType.STAR })
    }

    @Test
    fun treasuresInBounds_winterCrystal_activeInJanuary() {
        val treasures = generate(LocalDate.of(2027, 1, 10))

        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "EVENT_WINTER_CRYSTAL" })
    }

    @Test
    fun treasuresInBounds_newYearGoldenHunt_spansYearBoundary() {
        val date = LocalDate.of(2026, 1, 3)
        assertTrue(
            EventTreasureGenerator.isSupportedEventActive(
                type = SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
                evaluatedAt = date,
            ),
        )

        val newYearTreasures = generate(date).filter {
            it.placementKind == "EVENT_NEW_YEAR_GOLDEN_HUNT"
        }
        assertTrue(newYearTreasures.isNotEmpty())
        assertTrue(newYearTreasures.all { it.treasureId.contains(":new_year_golden_hunt:") })
    }

    @Test
    fun treasuresInBounds_summerExplorer_activeInJuly() {
        val treasures = generate(LocalDate.of(2026, 7, 15))

        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "EVENT_SUMMER_EXPLORER" })
    }

    @Test
    fun activeTreasureEvents_excludesUnsupportedCatalogEvents() {
        val active = EventTreasureGenerator.activeTreasureEvents(
            evaluatedAt = LocalDate.of(2026, 6, 7),
        )

        assertFalse(active.any { it.eventType == SeasonalEventType.ANNIVERSARY_EVENT })
        assertTrue(active.any { it.eventType == SeasonalEventType.SUMMER_EXPLORER })
    }

    @Test
    fun activeTreasureEvents_respectsExplorerLevelGate() {
        val active = EventTreasureGenerator.activeTreasureEvents(
            evaluatedAt = LocalDate.of(2026, 10, 20),
            explorerLevel = 5,
        )

        assertFalse(active.any { it.eventType == SeasonalEventType.HALLOWEEN_MYSTERY })
    }

    @Test
    fun treasuresInBounds_collectedIdsAreSkipped() {
        val date = LocalDate.of(2026, 4, 10)
        val baseline = generate(date)
        require(baseline.isNotEmpty())

        val collectedId = baseline.first().treasureId
        val filtered = EventTreasureGenerator.treasuresInBounds(
            bounds = bounds,
            region = region,
            isCollected = { it == collectedId },
            evaluatedAt = date,
        )

        assertFalse(filtered.any { it.treasureId == collectedId })
    }

    @Test
    fun profileCatalog_coversAllFiveSupportedEvents() {
        assertEquals(5, EventTreasureSchema.SUPPORTED_EVENT_TYPES.size)
        for (type in EventTreasureSchema.SUPPORTED_EVENT_TYPES) {
            assertTrue(EventTreasureProfileCatalog.isSupported(type))
            assertEquals(type, EventTreasureProfileCatalog.profileFor(type).eventType)
        }
    }

    private fun generate(
        date: LocalDate,
        tier: TreasureGenerationTier = TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT,
    ) = EventTreasureGenerator.treasuresInBounds(
        bounds = bounds,
        region = region,
        isCollected = { false },
        tier = tier,
        isInPlayRegion = { _, _ -> true },
        evaluatedAt = date,
    )
}
