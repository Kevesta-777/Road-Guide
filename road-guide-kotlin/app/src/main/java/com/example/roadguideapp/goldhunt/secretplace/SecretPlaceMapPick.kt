package com.example.roadguideapp.goldhunt.secretplace

import android.graphics.RectF
import com.example.roadguideapp.goldhunt.overlays.SecretPlaceOverlay
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

internal object SecretPlaceMapPick {
    private const val HIT_PAD_PX = 32f

    fun resolve(map: MapLibreMap, tapLatLng: LatLng): SecretPlaceSpec? {
        val screen = map.projection.toScreenLocation(tapLatLng)
        val box = RectF(
            screen.x - HIT_PAD_PX,
            screen.y - HIT_PAD_PX,
            screen.x + HIT_PAD_PX,
            screen.y + HIT_PAD_PX,
        )
        val features = map.queryRenderedFeatures(box, SecretPlaceOverlay.CIRCLE_LAYER_ID)
        if (features.isNullOrEmpty()) return null
        var best: SecretPlaceSpec? = null
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

    fun specFromFeature(feature: Feature): SecretPlaceSpec? {
        val props = feature.properties() ?: return null
        val secretPlaceId = props.get("secretPlaceId")?.asString ?: return null
        val type = SecretPlaceType.fromId(props.get("type")?.asString.orEmpty()) ?: return null
        val rarity = SecretPlaceRarity.fromId(props.get("rarity")?.asString.orEmpty()) ?: return null
        val displayName = props.get("displayName")?.asString ?: type.id
        val treasureNestSlots = props.get("treasureNestSlots")?.asInt ?: 0
        val regionL1Id = props.get("regionL1Id")?.asString ?: return null
        val regionL2Id = props.get("regionL2Id")?.asString ?: ""
        val point = feature.geometry() as? Point ?: return null
        return SecretPlaceSpec(
            secretPlaceId = secretPlaceId,
            type = type,
            rarity = rarity,
            lat = point.latitude(),
            lng = point.longitude(),
            regionL1Id = regionL1Id,
            regionL2Id = regionL2Id,
            displayName = displayName,
            treasureNestSlots = treasureNestSlots,
            storyFragmentId = null,
            placementKind = "MAP",
            generatorVersion = com.example.roadguideapp.goldhunt.GoldHuntConfig.SECRET_GENERATOR_VERSION,
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
