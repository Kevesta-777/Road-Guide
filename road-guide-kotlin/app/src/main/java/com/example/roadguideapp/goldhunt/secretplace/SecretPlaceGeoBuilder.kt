package com.example.roadguideapp.goldhunt.secretplace

import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

internal object SecretPlaceGeoBuilder {
    fun build(places: List<SecretPlaceSpec>): FeatureCollection {
        if (places.isEmpty()) return FeatureCollection.fromFeatures(emptyArray())
        val features = ArrayList<Feature>(places.size)
        for (spec in places) {
            val props = JsonObject().apply {
                addProperty("secretPlaceId", spec.secretPlaceId)
                addProperty("type", spec.type.id)
                addProperty("rarity", spec.rarity.id)
                addProperty("displayName", spec.displayName)
                addProperty("treasureNestSlots", spec.treasureNestSlots)
                addProperty("regionL1Id", spec.regionL1Id)
                addProperty("regionL2Id", spec.regionL2Id)
            }
            features += Feature.fromGeometry(
                Point.fromLngLat(spec.lng, spec.lat),
                props,
            )
        }
        return FeatureCollection.fromFeatures(features)
    }
}
