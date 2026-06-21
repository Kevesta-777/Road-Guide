package com.example.roadguideapp.map

import android.content.Context
import android.util.Log
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

internal object DirectionsNavigationVehicleLayer {
    private const val TAG = "NavVehicleLayer"
    private const val VEHICLE_SOURCE = "roadguide_nav_vehicle_src"
    private const val VEHICLE_LAYER = "roadguide_nav_vehicle"

    private var layersReady = false
    private var activeTravelMode: DirectionsTravelMode? = null
    private var activeImageId: String? = null

    private fun vehicleIconSizeExpression(): Expression = Expression.interpolate(
        Expression.exponential(1.45f),
        Expression.zoom(),
        Expression.stop(14, 0.46),
        Expression.stop(16, 0.62),
        Expression.stop(18, 0.80),
        Expression.stop(20, 0.95),
        Expression.stop(22, 1.08),
    )

    fun remove(style: Style) {
        layersReady = false
        activeTravelMode = null
        runCatching { style.removeLayer(VEHICLE_LAYER) }
        runCatching { style.removeSource(VEHICLE_SOURCE) }
        DirectionsTravelMode.entries.forEach { mode ->
            runCatching { style.removeImage(NavigationVehicleIcon.styleImageId(mode)) }
        }
        activeImageId = null
    }

    fun sync(
        style: Style,
        context: Context,
        lat: Double,
        lng: Double,
        bearingDegrees: Double,
        travelMode: DirectionsTravelMode,
    ) {
        try {
            if (activeTravelMode != travelMode) {
                layersReady = false
                activeTravelMode = travelMode
            }
            registerIcon(style, context, travelMode)
            val imageId = NavigationVehicleIcon.styleImageId(travelMode)
            val feature = Feature.fromGeometry(Point.fromLngLat(lng, lat))
            val anchor = topStackAnchorLayerId(style)

            if (!layersReady) {
                remove(style)
                activeTravelMode = travelMode
                registerIcon(style, context, travelMode)
                style.addSource(GeoJsonSource(VEHICLE_SOURCE, feature))
                val vehicleLayer = SymbolLayer(VEHICLE_LAYER, VEHICLE_SOURCE).withProperties(
                    PropertyFactory.iconImage(imageId),
                    PropertyFactory.iconSize(vehicleIconSizeExpression()),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconIgnorePlacement(true),
                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                    PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                    PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                    PropertyFactory.iconRotate(bearingDegrees.toFloat()),
                )
                addAbove(style, vehicleLayer, anchor)
                layersReady = true
                return
            }

            (style.getSource(VEHICLE_SOURCE) as? GeoJsonSource)?.setGeoJson(feature)
            (style.getLayer(VEHICLE_LAYER) as? SymbolLayer)?.setProperties(
                PropertyFactory.iconImage(imageId),
                PropertyFactory.iconRotate(bearingDegrees.toFloat()),
            )
        } catch (e: Exception) {
            Log.e(TAG, "sync vehicle failed", e)
            layersReady = false
        }
    }

    private fun registerIcon(
        style: Style,
        context: Context,
        travelMode: DirectionsTravelMode,
    ) {
        val imageId = NavigationVehicleIcon.styleImageId(travelMode)
        if (activeImageId == imageId) return
        activeImageId?.let { oldId -> runCatching { style.removeImage(oldId) } }
        activeImageId = imageId
        val bitmap = NavigationVehicleIcon.loadBitmap(context, travelMode)
        try {
            style.addImage(imageId, bitmap)
        } catch (_: Exception) {
            style.addImage(imageId, bitmap, true)
        }
    }

    /** Places the puck on top of route lines, roadguide overlays, and map symbol/glyph layers. */
    private fun topStackAnchorLayerId(style: Style): String? =
        style.layers.asReversed().firstOrNull { it.id != VEHICLE_LAYER }?.id

    private fun addAbove(style: Style, layer: org.maplibre.android.style.layers.Layer, aboveId: String?) {
        if (aboveId != null && style.getLayer(aboveId) != null) {
            style.addLayerAbove(layer, aboveId)
        } else {
            style.addLayer(layer)
        }
    }
}
