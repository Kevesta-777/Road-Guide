package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.treasure.TreasureHash
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.cos

internal object SecretPlaceGenerator {
    fun secretsInBounds(
        bounds: LatLngBounds,
        region: PlayRegion,
        isDiscovered: (String) -> Boolean,
        additionalSecretGeneration: Boolean = false,
    ): List<SecretPlaceSpec> {
        val regionId = TreasureGenerator.playRegionId(region)
        val spawnChance = if (additionalSecretGeneration) 1.0 else GoldHuntConfig.SECRET_PROCEDURAL_SPAWN_CHANCE
        val maxCells = if (additionalSecretGeneration) {
            GoldHuntConfig.MAX_SECRET_L2_CELLS_SCAN_UNLOCKED
        } else {
            GoldHuntConfig.MAX_SECRET_L2_CELLS_SCAN
        }
        val slotsPerL2 = if (additionalSecretGeneration) {
            GoldHuntConfig.SECRET_SLOTS_PER_L2_UNLOCKED
        } else {
            GoldHuntConfig.SECRET_SLOTS_PER_L2
        }
        val l2Cells = GridIndex.cellsInBounds(
            bounds = bounds,
            level = 2,
            region = region,
            maxCells = maxCells,
        )
        val catalogIds = emptySet<String>()
        val out = ArrayList<SecretPlaceSpec>(l2Cells.size * slotsPerL2)
        for (l2 in l2Cells) {
            for (slot in 0 until slotsPerL2) {
                if (out.size >= GoldHuntConfig.MAX_SECRET_FEATURES) return out
                val secretPlaceId = SecretPlaceId.forL2Slot(regionId, l2, slot)
                if (secretPlaceId in catalogIds || isDiscovered(secretPlaceId)) continue
                val seed = "$secretPlaceId:gen"
                val roll = TreasureHash.unitFraction("$seed:spawn")
                if (roll > spawnChance) continue
                val type = pickType(seed)
                val rarity = pickRarity(seed)
                val (lat, lng) = SecretPlacePlacement.placeInL2Cell(region, l2, slot, secretPlaceId)
                val l1 = GridIndex.parentL1(
                    GridIndex.encode(lat, lng, level = 0, region = region) ?: continue,
                    region,
                ) ?: continue
                out += SecretPlaceSpec(
                    secretPlaceId = secretPlaceId,
                    type = type,
                    rarity = rarity,
                    lat = lat,
                    lng = lng,
                    regionL1Id = l1.id,
                    regionL2Id = l2.id,
                    displayName = defaultName(type, rarity),
                    treasureNestSlots = nestSlotsFor(type, rarity),
                    storyFragmentId = null,
                    placementKind = "PROCEDURAL_L2",
                    generatorVersion = GoldHuntConfig.SECRET_GENERATOR_VERSION,
                )
            }
        }
        if (additionalSecretGeneration && out.size < GoldHuntConfig.SECRET_UNLOCKED_MIN_IN_VIEWPORT) {
            ensureMinimumInViewport(
                region = region,
                regionId = regionId,
                l2Cells = l2Cells,
                slotsPerL2 = slotsPerL2,
                catalogIds = catalogIds,
                isDiscovered = isDiscovered,
                out = out,
            )
        }
        return out
    }

    fun secretsNear(
        lat: Double,
        lng: Double,
        scanRadiusM: Double,
        region: PlayRegion,
        isDiscovered: (String) -> Boolean,
        additionalSecretGeneration: Boolean = false,
    ): List<SecretPlaceSpec> {
        val latDelta = scanRadiusM / 111_320.0
        val lngDelta = scanRadiusM / (111_320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2))
        val bounds = LatLngBounds.Builder()
            .include(LatLng(lat - latDelta, lng - lngDelta))
            .include(LatLng(lat + latDelta, lng + lngDelta))
            .build()
        return secretsInBounds(
            bounds = bounds,
            region = region,
            isDiscovered = isDiscovered,
            additionalSecretGeneration = additionalSecretGeneration,
        ).filter { spec ->
            ClusterGeoMath.distanceMeters(lat, lng, spec.lat, spec.lng) <= scanRadiusM
        }
    }

    private fun ensureMinimumInViewport(
        region: PlayRegion,
        regionId: String,
        l2Cells: List<GridCell>,
        slotsPerL2: Int,
        catalogIds: Set<String>,
        isDiscovered: (String) -> Boolean,
        out: ArrayList<SecretPlaceSpec>,
    ) {
        if (l2Cells.isEmpty()) return
        val targetAdds = GoldHuntConfig.SECRET_UNLOCKED_MIN_IN_VIEWPORT - out.size
        if (targetAdds <= 0) return
        var added = 0
        for (l2 in l2Cells) {
            if (added >= targetAdds || out.size >= GoldHuntConfig.MAX_SECRET_FEATURES) return
            for (slot in 0 until slotsPerL2) {
                val secretPlaceId = SecretPlaceId.forL2Slot(regionId, l2, slot)
                if (secretPlaceId in catalogIds || isDiscovered(secretPlaceId)) continue
                if (out.any { it.secretPlaceId == secretPlaceId }) continue
                val seed = "$secretPlaceId:forced"
                val type = pickType(seed)
                val rarity = pickRarity(seed)
                val (lat, lng) = SecretPlacePlacement.placeInL2Cell(region, l2, slot, secretPlaceId)
                val l1 = GridIndex.parentL1(
                    GridIndex.encode(lat, lng, level = 0, region = region) ?: continue,
                    region,
                ) ?: continue
                out += SecretPlaceSpec(
                    secretPlaceId = secretPlaceId,
                    type = type,
                    rarity = rarity,
                    lat = lat,
                    lng = lng,
                    regionL1Id = l1.id,
                    regionL2Id = l2.id,
                    displayName = defaultName(type, rarity),
                    treasureNestSlots = nestSlotsFor(type, rarity),
                    storyFragmentId = null,
                    placementKind = "PROCEDURAL_L2",
                    generatorVersion = GoldHuntConfig.SECRET_GENERATOR_VERSION,
                )
                added++
                if (added >= targetAdds) return
            }
        }
    }

    private fun pickType(seed: String): SecretPlaceType {
        val roll = TreasureHash.unitFraction("$seed:type")
        val types = SecretPlaceType.entries
        return types[(roll * types.size).toInt().coerceIn(0, types.lastIndex)]
    }

    private fun pickRarity(seed: String): SecretPlaceRarity {
        val roll = TreasureHash.unitFraction("$seed:rarity")
        return when {
            roll < 0.70 -> SecretPlaceRarity.COMMON
            roll < 0.92 -> SecretPlaceRarity.RARE
            else -> SecretPlaceRarity.LEGENDARY
        }
    }

    private fun defaultName(type: SecretPlaceType, rarity: SecretPlaceRarity): String =
        "${rarity.id.lowercase().replaceFirstChar { it.titlecase() }} ${type.id.lowercase().replace('_', ' ')}"

    private fun nestSlotsFor(type: SecretPlaceType, rarity: SecretPlaceRarity): Int = when (rarity) {
        SecretPlaceRarity.COMMON -> 1
        SecretPlaceRarity.RARE -> 2
        SecretPlaceRarity.LEGENDARY -> 3
    } + if (type == SecretPlaceType.TREASURE_NEST) 2 else 0
}
