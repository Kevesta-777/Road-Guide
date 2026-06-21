package com.example.roadguideapp.goldhunt.treasure.stats

import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarityGenerator
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureTileCoordinate
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureTypeRarity
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureWorldSeed

/**
 * Resolves the rarity tier recorded when a treasure is collected.
 * Uses deterministic rolls for L1 procedural ids; falls back to type defaults.
 */
internal object TreasureRarityResolver {
    private val l1TreasureIdPattern =
        Regex("""^tr:v\d+:(.+?):L1:(\d+):(\d+):s(\d+)$""")
    private val eventTreasureIdPattern =
        Regex("""^tr:event:v\d+:(.+?):([^:]+):(\d+):L1:(\d+):(\d+):s(\d+)$""")

    fun resolve(spec: TreasureSpec): TreasureRarity {
        resolveFromEventTreasureId(spec.treasureId)?.let { return it }
        val rolled = resolveFromTreasureId(spec.treasureId)
        return rolled ?: TreasureTypeRarity.rarityFor(spec.type)
    }

    fun resolveFromEventTreasureId(treasureId: String): TreasureRarity? {
        val match = eventTreasureIdPattern.matchEntire(treasureId) ?: return null
        val (_, eventTypeId, cycleYear, tileX, tileY, slot) = match.destructured
        val eventType = SeasonalEventType.entries.firstOrNull {
            it.name.equals(eventTypeId, ignoreCase = true) ||
                it.name.lowercase() == eventTypeId.lowercase()
        } ?: return null
        val tile = TreasureTileCoordinate(
            level = 1,
            tileX = tileX.toInt(),
            tileY = tileY.toInt(),
            slot = slot.toInt(),
        )
        val worldSeed = SeasonalEventSchema.SeedPrefixes.forInstance(
            type = eventType,
            cycleYear = cycleYear.toInt(),
        )
        return TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
    }

    fun resolveFromTreasureId(treasureId: String): TreasureRarity? {
        val match = l1TreasureIdPattern.matchEntire(treasureId) ?: return null
        val (playRegionId, tileX, tileY, slot) = match.destructured
        val tile = TreasureTileCoordinate(
            level = 1,
            tileX = tileX.toInt(),
            tileY = tileY.toInt(),
            slot = slot.toInt(),
        )
        val worldSeed = "${TreasureWorldSeed.DEFAULT}:$playRegionId"
        return TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
    }
}
