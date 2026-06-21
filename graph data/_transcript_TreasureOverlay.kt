package com.example.roadguideapp.goldhunt.overlays

import android.content.Context
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

/**
 * Uncollected treasure markers (sprite per type). Above base map, below routes and nav.
 */
internal object TreasureOverlay {
    private const val SOURCE_ID = "roadguide_treasure_src"
    const val SYMBOL_LAYER_ID = "roadguide_treasure_symbol"

    /** @deprecated Use [SYMBOL_LAYER_ID]; kept for tap hit-testing call sites. */
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
        GoldHuntMarkerIcons.ensureTreasureIcons(style, context.applicationContext)
        val density = context.applicationContext.resources.displayMetrics.density
        val source = GeoJsonSource(SOURCE_ID, features)
        val iconExpr = iconImageExpression()
        var symbol = SymbolLayer(SYMBOL_LAYER_ID, SOURCE_ID)
            .withProperties(PropertyFactory.iconImage(iconExpr))
        symbol = GoldHuntTreasureMapStyle.applySymbolStyle(symbol, display3d, density)
        try {
            style.addSource(source)
            if (display3d) {
                val shadow = GoldHuntTreasureMapStyle.groundShadowLayer(shadowLayerId, SOURCE_ID)
                GoldHuntOverlayPlacement.insertAboveBaseMap(style, shadow)
                style.addLayerAbove(symbol, shadowLayerId)
            } else {
                GoldHuntOverlayPlacement.insertAboveFog(style, symbol)
            }
        } catch (_: Exception) {
            runCatching {
                style.addSource(source)
                if (display3d) {
                    val shadow = GoldHuntTreasureMapStyle.groundShadowLayer(shadowLayerId, SOURCE_ID)
                    GoldHuntOverlayPlacement.insertAboveFog(style, shadow)
                    style.addLayerAbove(symbol, shadowLayerId)
                } else {
                    GoldHuntOverlayPlacement.insertAboveFog(style, symbol)
                }
            }
        }
    }

    private fun iconImageExpression(): Expression = Expression.match(
        Expression.get("type"),
        Expression.literal(TreasureType.STAR.id),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.STAR)),
        Expression.literal(TreasureType.FLOWER.id),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.FLOWER)),
        Expression.literal(TreasureType.CRYSTAL.id),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.CRYSTAL)),
        Expression.literal(TreasureType.GIFT.id),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.GIFT)),
        Expression.literal(TreasureType.HINT.id),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.HINT)),
        Expression.literal(GoldHuntMarkerIcons.treasureStyleImageId(TreasureType.STAR)),
    )
}
