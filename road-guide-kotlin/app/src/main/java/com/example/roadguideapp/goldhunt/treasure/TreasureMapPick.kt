package com.example.roadguideapp.goldhunt.treasure

import android.graphics.RectF
import com.example.roadguideapp.goldhunt.overlays.TreasureOverlay
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

internal object TreasureMapPick {
    private const val HIT_PAD_PX = 28f

    fun resolve(map: MapLibreMap, tapLatLng: LatLng): TreasureSpec? {
        val screen = map.projection.toScreenLocation(tapLatLng)
        val box = RectF(
            screen.x - HIT_PAD_PX,
            screen.y - HIT_PAD_PX,
            screen.x + HIT_PAD_PX,
            screen.y + HIT_PAD_PX,
        )
        val features = map.queryRenderedFeatures(box, TreasureOverlay.CIRCLE_LAYER_ID)
        if (features.isNullOrEmpty()) return null
        var best: TreasureSpec? = null
        var bestDist = Double.MAX_VALUE
        for (feature in features) {
            val spec = specFromFeature(feature) ?: continue
            val dist = distanceMeters(tapLatLng, spec.lat, spec.lng)
            if (dist < bestDist) {
                bestDist = dist
                best = spec
            }
        }
        return best
    }

    fun specFromFeature(feature: Feature): TreasureSpec? {
        val props = feature.properties() ?: return null
        val treasureId = props.get("treasureId")?.asString ?: return null
        val typeId = props.get("type")?.asString ?: return null
        val type = TreasureType.fromId(typeId) ?: return null
        val creditAmount = props.get("creditAmount")?.asInt
            ?: return null
        val placementKind = props.get("placementKind")?.asString ?: "GRID"
        val regionL1Id = props.get("regionL1Id")?.asString ?: return null
        val point = feature.geometry() as? Point ?: return null
        return TreasureSpec(
            treasureId = treasureId,
            type = type,
            lat = point.latitude(),
            lng = point.longitude(),
            creditAmount = creditAmount,
            placementKind = placementKind,
            regionL1Id = regionL1Id,
        )
    }

    private fun distanceMeters(tap: LatLng, lat: Double, lng: Double): Double {
        val dLat = Math.toRadians(lat - tap.latitude)
        val dLng = Math.toRadians(lng - tap.longitude)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(tap.latitude)) *
            kotlin.math.cos(Math.toRadians(lat)) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        return 6_371_000.0 * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}
