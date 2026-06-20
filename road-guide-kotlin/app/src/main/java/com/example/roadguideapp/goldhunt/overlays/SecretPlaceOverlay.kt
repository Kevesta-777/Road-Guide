package com.example.roadguideapp.goldhunt.overlays

import android.content.Context
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

internal object SecretPlaceOverlay {
    private const val SOURCE_ID = "roadguide_secret_place_src"
    const val SYMBOL_LAYER_ID = "roadguide_secret_place_symbol"

    /** @deprecated Use [SYMBOL_LAYER_ID]. */
    const val CIRCLE_LAYER_ID: String = SYMBOL_LAYER_ID

    private val shadowLayerId: String
        get() = GoldHuntTreasureMapStyle.groundShadowLayerId(SYMBOL_LAYER_ID)

    fun remove(style: Style) {
        runCatching { style.removeLayer(SYMBOL_LAYER_ID) }
        runCatching { style.removeLayer(shadowLayerId) }
        runCatching { style.removeSource(SOURCE_ID) }
    }

    fun sync(
        style: Style,
        context: Context,
        features: FeatureCollection,
        display3d: Boolean,
    ) {
        remove(style)
        if (features.features().isNullOrEmpty()) return
        GoldHuntMarkerIcons.ensureSecretPlaceIcon(style, context.applicationContext)
        val density = context.applicationContext.resources.displayMetrics.density
        val source = GeoJsonSource(SOURCE_ID, features)
        var symbol = SymbolLayer(SYMBOL_LAYER_ID, SOURCE_ID)
            .withProperties(
                PropertyFactory.iconImage(GoldHuntMarkerIcons.secretPlaceStyleImageId()),
            )
        symbol = GoldHuntTreasureMapStyle.applySymbolStyle(symbol, display3d, density)
        val treasureAnchor = TreasureOverlay.SYMBOL_LAYER_ID
        try {
            style.addSource(source)
            if (display3d) {
                val shadow = GoldHuntTreasureMapStyle.groundShadowLayer(shadowLayerId, SOURCE_ID)
                GoldHuntOverlayPlacement.insertAboveLayerIfPresent(style, shadow, treasureAnchor)
                style.addLayerAbove(symbol, shadowLayerId)
            } else {
                GoldHuntOverlayPlacement.insertAboveLayerIfPresent(style, symbol, treasureAnchor)
            }
        } catch (_: Exception) {
            runCatching {
                style.addSource(source)
                if (display3d) {
                    val shadow = GoldHuntTreasureMapStyle.groundShadowLayer(shadowLayerId, SOURCE_ID)
                    GoldHuntOverlayPlacement.insertAboveLayerIfPresent(style, shadow, treasureAnchor)
                    style.addLayerAbove(symbol, shadowLayerId)
                } else {
                    GoldHuntOverlayPlacement.insertAboveLayerIfPresent(style, symbol, treasureAnchor)
                }
            }
        }
    }
}
