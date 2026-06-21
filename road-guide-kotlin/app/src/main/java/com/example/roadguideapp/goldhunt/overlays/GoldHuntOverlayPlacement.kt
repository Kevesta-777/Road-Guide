package com.example.roadguideapp.goldhunt.overlays

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Layer

/** Places Gold Hunt overlays above the base map style, below routes and navigation. */
internal object GoldHuntOverlayPlacement {
    fun insertAboveBaseMap(style: Style, layer: Layer) {
        val anchorId = findBaseMapTopLayerId(style)
        when {
            anchorId != null && style.getLayer(anchorId) != null -> {
                style.addLayerAbove(layer, anchorId)
            }
            else -> {
                style.addLayer(layer)
            }
        }
    }

    fun insertAboveLayerIfPresent(style: Style, layer: Layer, anchorLayerId: String) {
        if (style.getLayer(anchorLayerId) != null) {
            style.addLayerAbove(layer, anchorLayerId)
        } else {
            insertAboveBaseMap(style, layer)
        }
    }

    private fun findBaseMapTopLayerId(style: Style): String? {
        val layers = style.layers
        var lastNonRoadGuide: String? = null
        for (layer in layers) {
            val id = layer.id
            if (id.startsWith("roadguide_")) break
            lastNonRoadGuide = id
        }
        return lastNonRoadGuide
    }
}
