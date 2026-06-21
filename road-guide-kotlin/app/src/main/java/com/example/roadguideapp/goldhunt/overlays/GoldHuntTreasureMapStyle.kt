package com.example.roadguideapp.goldhunt.overlays

import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer

/**
 * Map display for Gold Hunt markers: flat sprites in 2D, billboard + ground shadow in 3D.
 */
internal object GoldHuntTreasureMapStyle {
    const val GROUND_SHADOW_LAYER_SUFFIX = "_ground_shadow"

    private const val MIN_TILT_FOR_3D_DISPLAY_DEG = 12.0

    fun isDisplay3d(map3dEnabled: Boolean, cameraTiltDeg: Double): Boolean =
        map3dEnabled && cameraTiltDeg >= MIN_TILT_FOR_3D_DISPLAY_DEG

    fun groundShadowLayerId(symbolLayerId: String): String =
        symbolLayerId + GROUND_SHADOW_LAYER_SUFFIX

    fun groundShadowLayer(layerId: String, sourceId: String): CircleLayer =
        CircleLayer(layerId, sourceId).withProperties(
            PropertyFactory.circleRadius(shadowRadiusExpression()),
            PropertyFactory.circleColor(Expression.literal("#59000000")),
            PropertyFactory.circleBlur(0.9f),
            PropertyFactory.circleOpacity(0.62f),
            PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
            PropertyFactory.circleTranslate(shadowOffsetExpression()),
            PropertyFactory.circleTranslateAnchor(Property.CIRCLE_TRANSLATE_ANCHOR_MAP),
        )

    fun applySymbolStyle(
        layer: SymbolLayer,
        display3d: Boolean,
        density: Float,
    ): SymbolLayer {
        val props = if (display3d) {
            arrayOf(
                PropertyFactory.iconSize(GoldHuntMarkerIcons.DISPLAY_ICON_SCALE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_VIEWPORT),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_VIEWPORT),
                PropertyFactory.iconTranslate(arrayOf(0f, iconLiftPx(density))),
                PropertyFactory.iconTranslateAnchor(Property.ICON_TRANSLATE_ANCHOR_VIEWPORT),
                PropertyFactory.symbolSortKey(2f),
            )
        } else {
            arrayOf(
                PropertyFactory.iconSize(GoldHuntMarkerIcons.DISPLAY_ICON_SCALE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                PropertyFactory.symbolSortKey(1f),
            )
        }
        return layer.withProperties(*props)
    }

    fun iconLiftPx(density: Float): Float =
        (-16f * density.coerceIn(1f, 4f)).coerceIn(-30f, -10f)

    /** Screen-space offset from geographic anchor to visual icon center (matches symbol layer). */
    fun iconCenterOffsetYPx(display3d: Boolean, density: Float): Float {
        if (!display3d) return 0f
        val lift = iconLiftPx(density)
        val halfIcon = 34f * density * GoldHuntMarkerIcons.DISPLAY_ICON_SCALE
        return lift - halfIcon
    }

    private fun shadowRadiusExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(17, 11f),
        Expression.stop(18, 15f),
        Expression.stop(19, 19f),
        Expression.stop(20, 23f),
    )

    /** Slight map-space offset so the shadow sits “under” the billboard in tilted view. */
    private fun shadowOffsetExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(17, Expression.literal(arrayOf(0f, 3f))),
        Expression.stop(19, Expression.literal(arrayOf(0f, 5f))),
        Expression.stop(20, Expression.literal(arrayOf(0f, 6f))),
    )
}
