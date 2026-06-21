package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterGenerator
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterDistributionHints
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterTreasureDistributionEngine
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.cos

internal object TreasureGenerator {
    fun playRegionId(region: PlayRegion): String =
        "r:${"%.4f".format(region.west)}:${"%.4f".format(region.south)}"

    fun treasuresInBounds(
        bounds: LatLngBounds,
        region: PlayRegion,
        isCollected: (String) -> Boolean,
        tier: TreasureGenerationTier = TreasureGenerationTier.STAR_ONLY,
        isInPlayRegion: (Double, Double) -> Boolean = { _, _ -> true },
    ): List<TreasureSpec> {
        val regionId = playRegionId(region)
        val l1Cells = GridIndex.cellsInBounds(
            bounds = bounds,
            level = 1,
            region = region,
            maxCells = GoldHuntConfig.MAX_TREASURE_L1_CELLS_SCAN,
        )
        val out = ArrayList<TreasureSpec>(l1Cells.size * GoldHuntConfig.SLOTS_PER_L1)
        for (l1 in l1Cells) {
            for (slot in 0 until GoldHuntConfig.SLOTS_PER_L1) {
                if (out.size >= GoldHuntConfig.MAX_TREASURE_FEATURES) return out
                val treasureId = TreasureId.forL1Slot(regionId, l1, slot)
                if (isCollected(treasureId)) continue
                val seed = "$treasureId:gen"
                val roll = TreasureHash.unitFraction("$seed:spawn")
                if (roll > GoldHuntConfig.TREASURE_PROCEDURAL_SPAWN_CHANCE) continue
                val type = TreasureTypePicker.pick(seed, tier)
                val (lat, lng) = TreasurePlacementEngine.placeInL1Cell(region, l1, slot, treasureId)
                if (!isInPlayRegion(lat, lng)) continue
                val credit = rollCredits(type, seed)
                out += TreasureSpec(
                    treasureId = treasureId,
                    type = type,
                    lat = lat,
                    lng = lng,
                    creditAmount = credit,
                    placementKind = "PROCEDURAL_L1",
                    regionL1Id = l1.id,
                )
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
    ): List<TreasureSpec> {
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2)
        val dLat = radiusM / metersPerDegLat
        val dLng = radiusM / metersPerDegLng
        val bounds = LatLngBounds.Builder()
            .include(org.maplibre.android.geometry.LatLng(lat - dLat, lng - dLng))
            .include(org.maplibre.android.geometry.LatLng(lat + dLat, lng + dLng))
            .build()
        return treasuresInBounds(bounds, region, isCollected, tier, isInPlayRegion)
    }

    fun clusterTreasuresInBounds(
        bounds: LatLngBounds,
        region: PlayRegion,
        tier: TreasureGenerationTier,
        isCollected: (String) -> Boolean,
        hints: ClusterDistributionHints = ClusterDistributionHints.EMPTY,
        isInPlayRegion: (Double, Double) -> Boolean = { _, _ -> true },
        isClusterCompleted: (String) -> Boolean = { false },
    ): List<TreasureSpec> {
        val clusters = TreasureClusterGenerator.clustersInBounds(
            bounds = bounds,
            region = region,
            isClusterCompleted = isClusterCompleted,
        )
        if (clusters.isEmpty()) return emptyList()
        val out = ArrayList<TreasureSpec>()
        for (cluster in clusters) {
            out += ClusterTreasureDistributionEngine.distribute(
                cluster = cluster,
                region = region,
                tier = tier,
                hints = hints,
                isCollected = isCollected,
                isInPlayRegion = isInPlayRegion,
            )
        }
        return out
    }

    internal fun rollCredits(type: TreasureType, seed: String): Int = when (type) {
        TreasureType.STAR -> TreasureHash.intInRange(
            "$seed:credit",
            GoldHuntConfig.TREASURE_STAR_CREDIT_MIN,
            GoldHuntConfig.TREASURE_STAR_CREDIT_MAX,
        )
        TreasureType.FLOWER -> TreasureHash.intInRange(
            "$seed:credit",
            GoldHuntConfig.TREASURE_FLOWER_CREDIT_MIN,
            GoldHuntConfig.TREASURE_FLOWER_CREDIT_MAX,
        )
        TreasureType.CRYSTAL -> TreasureHash.intInRange(
            "$seed:credit",
            GoldHuntConfig.TREASURE_CRYSTAL_CREDIT_MIN,
            GoldHuntConfig.TREASURE_CRYSTAL_CREDIT_MAX,
        )
        TreasureType.GIFT -> TreasureHash.intInRange(
            "$seed:credit",
            GoldHuntConfig.TREASURE_GIFT_CREDIT_MIN,
            GoldHuntConfig.TREASURE_GIFT_CREDIT_MAX,
        )
        TreasureType.HINT -> 0
    }
}
