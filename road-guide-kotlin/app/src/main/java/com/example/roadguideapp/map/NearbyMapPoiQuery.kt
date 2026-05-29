package com.example.roadguideapp.map

import android.content.Context
import android.graphics.RectF
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature

/** A nearby category hit tied to a vector-tile POI when available. */
internal data class NearbyMapHighlight(
    val result: PeliasSearchResult,
    val pick: MapPlacePick?,
)

/**
 * Finds POIs already rendered on the visible map (vector tiles) and filters them by nearby
 * category — not Pelias text search for a single named place.
 */
internal object NearbyMapPoiQuery {

    const val MAX_RESULTS = 40

    fun queryVisibleCategory(
        context: Context,
        style: Style,
        map: MapLibreMap,
        mapView: MapView,
        category: AppleNearbyShortcut,
    ): List<NearbyMapHighlight> {
        val width = mapView.width.toFloat()
        val height = mapView.height.toFloat()
        if (width <= 0f || height <= 0f) return emptyList()

        val screenBox = RectF(0f, 0f, width, height)
        val layerIds = MapPoiSelectionController.poiTemplateLayerIds(style)
        if (layerIds.isEmpty()) return emptyList()

        val center = map.cameraPosition.target ?: return emptyList()
        val seen = HashSet<String>()
        val ranked = ArrayList<Triple<NearbyMapHighlight, Double, MapPlacePick>>()

        for (layerId in layerIds) {
            val features = map.queryRenderedFeatures(screenBox, layerId)
            for (feature in features) {
                if (!NearbyCategorySearch.matchesMapFeature(category, feature)) continue
                val pick = toPick(context, feature, center, layerId) ?: continue
                if (!seen.add(pick.detail.id)) continue
                ranked.add(
                    Triple(
                        NearbyMapHighlight(result = pick.toSearchResult(), pick = pick),
                        DirectionsPathOptimizer.haversineMeters(center, pick.detail.latLng),
                        pick,
                    ),
                )
            }
        }

        return ranked
            .sortedBy { it.second }
            .take(MAX_RESULTS)
            .map { it.first }
    }

    fun resolvePicksForResults(
        context: Context,
        style: Style,
        map: MapLibreMap,
        results: List<PeliasSearchResult>,
    ): List<MapPlacePick> {
        val picks = ArrayList<MapPlacePick>(results.size)
        val seen = HashSet<String>()
        for (result in results) {
            val pick = MapPoiSelectionController.resolvePickNear(context, style, map, result.latLng)
                ?: continue
            if (seen.add(pick.detail.id)) {
                picks.add(pick)
            }
        }
        return picks
    }

    private fun toPick(
        context: Context,
        feature: Feature,
        fallback: LatLng,
        layerId: String,
    ): MapPlacePick? {
        val detail = MapPlaceDetail.fromMapFeature(context, feature, fallback) ?: return null
        return MapPlacePick(detail = detail, feature = feature, templateLayerId = layerId)
    }
}

private fun MapPlacePick.toSearchResult(): PeliasSearchResult =
    PeliasSearchResult(
        gid = detail.id,
        label = detail.address.ifBlank { detail.name },
        name = detail.name,
        layer = "venue",
        latitude = detail.latLng.latitude,
        longitude = detail.latLng.longitude,
    )
