package com.example.roadguideapp.goldhunt.treasure

import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

internal object TreasureGeoBuilder {
    fun build(treasures: List<TreasureSpec>): FeatureCollection {
        if (treasures.isEmpty()) return FeatureCollection.fromFeatures(emptyArray())
        val features = ArrayList<Feature>(treasures.size)
        for (spec in treasures) {
            val props = JsonObject().apply {
                addProperty("treasureId", spec.treasureId)
                addProperty("type", spec.type.id)
                addProperty("creditAmount", spec.creditAmount)
                addProperty("placementKind", spec.placementKind)
                addProperty("regionL1Id", spec.regionL1Id)
            }
            features += Feature.fromGeometry(
                Point.fromLngLat(spec.lng, spec.lat),
                props,
            )
        }
        return FeatureCollection.fromFeatures(features)
    }
}
