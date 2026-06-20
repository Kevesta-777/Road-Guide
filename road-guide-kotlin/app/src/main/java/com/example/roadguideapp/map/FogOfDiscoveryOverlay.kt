package com.example.roadguideapp.map

import com.example.roadguideapp.goldhunt.DiscoveryConfig
import com.example.roadguideapp.goldhunt.DiscoveryGrid
import com.example.roadguideapp.goldhunt.storage.DiscoveryStore
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

/**
 * MapLibre fog layer: semi-transparent fill over undiscovered grid cells in the viewport.
 */
internal object FogOfDiscoveryOverlay {

    private const val SOURCE_ID = "roadguide_fog_undiscovered_src"
    private const val LAYER_ID = "roadguide_fog_undiscovered_fill"
    private const val FOG_COLOR = "#12121F"

    fun remove(style: Style) {
        runCatching { style.removeLayer(LAYER_ID) }
        runCatching { style.removeSource(SOURCE_ID) }
    }

    fun sync(
        style: Style,
        store: DiscoveryStore,
        viewportBounds: LatLngBounds?,
        regionBounds: LatLngBounds?,
        mapZoom: Double,
        enabled: Boolean,
        fogOpacity: Float,
    ) {
        if (!enabled || viewportBounds == null || regionBounds == null) {
            remove(style)
            return
        }
        val step = DiscoveryGrid.renderStepMultiplier(mapZoom)
        val features = runCatching {
            buildUndiscoveredFeatures(store, viewportBounds, regionBounds, step)
        }.getOrElse { emptyList() }
        if (features.isEmpty()) {
            remove(style)
            return
        }
        val collection = FeatureCollection.fromFeatures(features)
        val existing = style.getSource(SOURCE_ID) as? GeoJsonSource
        if (existing != null) {
            existing.setGeoJson(collection)
            (style.getLayer(LAYER_ID) as? FillLayer)?.setProperties(
                PropertyFactory.fillOpacity(fogOpacity),
            )
            return
        }
        remove(style)
        val source = GeoJsonSource(SOURCE_ID, collection)
        val layer = FillLayer(LAYER_ID, SOURCE_ID).withProperties(
            PropertyFactory.fillColor(FOG_COLOR),
            PropertyFactory.fillOpacity(fogOpacity),
            PropertyFactory.fillOutlineColor(FOG_COLOR),
            PropertyFactory.fillAntialias(true),
        )
        val anchor = fogAnchorLayerId(style)
        try {
            style.addSource(source)
            if (anchor != null && style.getLayer(anchor) != null) {
                style.addLayerAbove(layer, anchor)
            } else {
                style.addLayer(layer)
            }
        } catch (_: Exception) {
            runCatching {
                style.addSource(source)
                style.addLayer(layer)
            }
        }
    }

    private fun buildUndiscoveredFeatures(
        store: DiscoveryStore,
        viewportBounds: LatLngBounds,
        regionBounds: LatLngBounds,
        stepMultiplier: Int,
    ): List<Feature> {
        val features = ArrayList<Feature>(512)
        for (cellId in DiscoveryGrid.cellsInViewport(viewportBounds, regionBounds, stepMultiplier)) {
            if (store.isDiscovered(cellId)) continue
            val feature = cellPolygonFeature(cellId, stepMultiplier) ?: continue
            features.add(feature)
            if (features.size >= DiscoveryConfig.MAX_FOG_FEATURES_PER_SYNC) break
        }
        return features
    }

    private fun cellPolygonFeature(cellId: Long, stepMultiplier: Int): Feature? {
        val cellBounds = DiscoveryGrid.cellBounds(cellId, stepMultiplier) ?: return null
        val ring = listOf(
            Point.fromLngLat(cellBounds.longitudeWest, cellBounds.latitudeSouth),
            Point.fromLngLat(cellBounds.longitudeEast, cellBounds.latitudeSouth),
            Point.fromLngLat(cellBounds.longitudeEast, cellBounds.latitudeNorth),
            Point.fromLngLat(cellBounds.longitudeWest, cellBounds.latitudeNorth),
            Point.fromLngLat(cellBounds.longitudeWest, cellBounds.latitudeSouth),
        )
        return Feature.fromGeometry(Polygon.fromLngLats(listOf(ring)))
    }

    private fun fogAnchorLayerId(style: Style): String? {
        val candidates = listOf(
            "landcover",
            PmtilesOverviewStylePatch.overviewLayerId("landcover"),
            "landuse",
            PmtilesOverviewStylePatch.overviewLayerId("landuse"),
            "water",
            PmtilesOverviewStylePatch.overviewLayerId("water"),
        )
        return candidates.firstOrNull { style.getLayer(it) != null }
    }
}
