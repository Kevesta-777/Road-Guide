package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.events.SeasonalEventActivationEngine
import com.example.roadguideapp.goldhunt.events.SeasonalEventDeviceClock
import com.example.roadguideapp.goldhunt.events.SeasonalEventLevelGate
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureHash
import com.example.roadguideapp.goldhunt.treasure.TreasurePlacementEngine
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.maplibre.android.geometry.LatLngBounds
import java.time.LocalDate
import kotlin.math.cos

/**
 * Generates deterministic seasonal event treasures while catalog events are active.
 *
 * Uses device date + [SeasonalEventActivationEngine] schedule; no network required.
 */
internal object EventTreasureGenerator {
    fun treasuresInBounds(
        bounds: LatLngBounds,
        region: PlayRegion,
        isCollected: (String) -> Boolean,
        tier: TreasureGenerationTier = TreasureGenerationTier.STAR_ONLY,
        isInPlayRegion: (Double, Double) -> Boolean = { _, _ -> true },
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): List<TreasureSpec> {
        val activeEvents = activeTreasureEvents(evaluatedAt, explorerLevel)
        if (activeEvents.isEmpty()) return emptyList()

        val regionId = TreasureGenerator.playRegionId(region)
        val l1Cells = GridIndex.cellsInBounds(
            bounds = bounds,
            level = 1,
            region = region,
            maxCells = GoldHuntConfig.MAX_TREASURE_L1_CELLS_SCAN,
        )
        val out = ArrayList<TreasureSpec>(
            l1Cells.size * EventTreasureSchema.SLOTS_PER_L1 * activeEvents.size,
        )

        for (event in activeEvents) {
            val profile = EventTreasureProfileCatalog.profileFor(event.eventType)
            for (l1 in l1Cells) {
                for (slot in 0 until EventTreasureSchema.SLOTS_PER_L1) {
                    if (out.size >= EventTreasureSchema.MAX_FEATURES) return out
                    val treasureId = EventTreasureId.forL1Slot(
                        playRegionId = regionId,
                        eventType = event.eventType,
                        cycleYear = event.cycleYear,
                        l1 = l1,
                        slot = slot,
                    )
                    if (isCollected(treasureId)) continue
                    val seed = EventTreasureSchema.SeedPrefixes.forSlot(event.eventSeed, treasureId)
                    val spawnRoll = TreasureHash.unitFraction("$seed:spawn")
                    if (spawnRoll > profile.spawnChance) continue
                    val type = EventTreasureTypePicker.pick(seed, profile, tier)
                    val (lat, lng) = TreasurePlacementEngine.placeInL1Cell(region, l1, slot, treasureId)
                    if (!isInPlayRegion(lat, lng)) continue
                    val credit = rollCredits(type, seed, profile.creditMultiplier)
                    out += TreasureSpec(
                        treasureId = treasureId,
                        type = type,
                        lat = lat,
                        lng = lng,
                        creditAmount = credit,
                        placementKind = profile.placementKind,
                        regionL1Id = l1.id,
                    )
                }
            }
        }
        return out
    }

    fun nearLatLng(
        lat: Double,
        lng: Double,
        radiusM: Double,
        region: PlayRegion,
        isCollected: (String) -> Boolean,
        tier: TreasureGenerationTier = TreasureGenerationTier.STAR_ONLY,
        isInPlayRegion: (Double, Double) -> Boolean = { _, _ -> true },
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): List<TreasureSpec> {
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2)
        val dLat = radiusM / metersPerDegLat
        val dLng = radiusM / metersPerDegLng
        val bounds = LatLngBounds.Builder()
            .include(org.maplibre.android.geometry.LatLng(lat - dLat, lng - dLng))
            .include(org.maplibre.android.geometry.LatLng(lat + dLat, lng + dLng))
            .build()
        return treasuresInBounds(
            bounds = bounds,
            region = region,
            isCollected = isCollected,
            tier = tier,
            isInPlayRegion = isInPlayRegion,
            evaluatedAt = evaluatedAt,
            explorerLevel = explorerLevel,
        )
    }

    fun activeTreasureEvents(
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): List<SeasonalEvent> {
        val activation = SeasonalEventActivationEngine.activate(evaluatedAt)
        val supported = activation.activeCatalogEvents.filter { event ->
            EventTreasureProfileCatalog.isSupported(event.eventType)
        }
        return if (explorerLevel != null) {
            SeasonalEventLevelGate.filterAccessible(supported, explorerLevel)
        } else {
            supported
        }
    }

    fun isSupportedEventActive(
        type: SeasonalEventType,
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): Boolean = activeTreasureEvents(evaluatedAt, explorerLevel).any { it.eventType == type }

    private fun rollCredits(
        type: TreasureType,
        seed: String,
        creditMultiplier: Double,
    ): Int {
        val base = TreasureGenerator.rollCredits(type, seed)
        if (creditMultiplier <= 1.0) return base
        return (base * creditMultiplier).toInt().coerceAtLeast(base)
    }
}
