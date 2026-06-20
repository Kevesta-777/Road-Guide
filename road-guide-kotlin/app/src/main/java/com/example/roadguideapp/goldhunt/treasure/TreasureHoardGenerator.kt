package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.cos
import kotlin.math.sin

internal object TreasureHoardGenerator {
    fun treasuresForViewport(
        hoard: TreasureHoardRegion,
        bounds: LatLngBounds,
        region: PlayRegion,
        tier: TreasureGenerationTier,
        isCollected: (String) -> Boolean,
    ): List<TreasureSpec> {
        if (!viewportIntersectsHoard(bounds, hoard)) return emptyList()
        val regionId = TreasureGenerator.playRegionId(region)
        val hoardL1Id = "hoard:${hoard.regionName}:${hoard.seed}"
        val count = hoard.treasureCount.coerceIn(1, GoldHuntConfig.HOARD_TREASURE_COUNT)
        val out = ArrayList<TreasureSpec>(count)
        for (index in 0 until count) {
            val treasureId = TreasureId.forHoard(regionId, hoard, index)
            if (isCollected(treasureId)) continue
            val seed = "$treasureId:gen"
            val type = TreasureTypePicker.pick(seed, tier)
            val (lat, lng) = placeNearCenter(hoard, treasureId)
            val credit = rollCredits(type, seed)
            out += TreasureSpec(
                treasureId = treasureId,
                type = type,
                lat = lat,
                lng = lng,
                creditAmount = credit,
                placementKind = "HOARD",
                regionL1Id = hoardL1Id,
            )
        }
        return out
    }

    private fun viewportIntersectsHoard(bounds: LatLngBounds, hoard: TreasureHoardRegion): Boolean {
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(hoard.lat)).coerceAtLeast(0.2)
        val padLat = GoldHuntConfig.HOARD_VISIBILITY_RADIUS_M / metersPerDegLat
        val padLng = GoldHuntConfig.HOARD_VISIBILITY_RADIUS_M / metersPerDegLng
        val hoardSouth = hoard.lat - padLat
        val hoardNorth = hoard.lat + padLat
        val hoardWest = hoard.lng - padLng
        val hoardEast = hoard.lng + padLng
        return bounds.latitudeNorth >= hoardSouth &&
            bounds.latitudeSouth <= hoardNorth &&
            bounds.longitudeEast >= hoardWest &&
            bounds.longitudeWest <= hoardEast
    }

    private fun placeNearCenter(hoard: TreasureHoardRegion, treasureId: String): Pair<Double, Double> {
        val angle = TreasureHash.unitFraction("$treasureId:angle") * 2.0 * Math.PI
        val radiusFrac = TreasureHash.unitFraction("$treasureId:radius")
        val radiusM = radiusFrac * GoldHuntConfig.HOARD_RADIUS_M
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(hoard.lat)).coerceAtLeast(0.2)
        val dLat = radiusM * sin(angle) / metersPerDegLat
        val dLng = radiusM * cos(angle) / metersPerDegLng
        return hoard.lat + dLat to hoard.lng + dLng
    }

    private fun rollCredits(type: TreasureType, seed: String): Int = when (type) {
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
