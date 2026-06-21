package com.example.roadguideapp.map

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory

/**
 * Applies [MapPalette] road colors to all Headway/OSM-Liberty transportation line layers.
 *
 * Headway `basic.json` uses per-class layer ids (`road_motorway`, `tunnel_street_casing`, …),
 * not the legacy OpenMapTiles ids `transportation` / `transportation-casing`.
 */
internal object MapTransportationStyle {

    private const val SOURCE_LAYER_TRANSPORTATION = "transportation"
    private const val SOURCE_LAYER_AEROWAY = "aeroway"

    fun applyRoadPalette(style: Style, palette: MapPalette) {
        for (layer in style.layers) {
            if (layer !is LineLayer) continue
            val sourceLayer = layer.transportationSourceLayerOrNull() ?: continue
            val color = when (sourceLayer) {
                SOURCE_LAYER_TRANSPORTATION -> lineColorForTransportationLayer(layer.id, palette)
                SOURCE_LAYER_AEROWAY -> palette.road
                else -> null
            } ?: continue
            layer.setProperties(PropertyFactory.lineColor(color))
        }
    }

    /**
     * Classifies Headway transportation line layer ids (including `overview_` PMTiles clones).
     */
    internal fun lineColorForTransportationLayer(layerId: String, palette: MapPalette): String {
        val id = layerId.lowercase()
        return when {
            id.contains("casing") -> palette.roadCasing
            id.contains("rail") || id.contains("hatching") -> palette.roadRail
            id.contains("path") || id.contains("pedestrian") -> palette.roadPath
            id.contains("ferry") -> palette.roadFerry
            else -> palette.road
        }
    }

    private fun LineLayer.transportationSourceLayerOrNull(): String? =
        runCatching { sourceLayer }.getOrNull()?.takeIf { it.isNotBlank() }
}
