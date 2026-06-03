package com.example.roadguideapp.map

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

    private fun vehicleIconSizeExpression(): Expression = Expression.interpolate(
        Expression.exponential(1.45f),
        Expression.zoom(),
        Expression.stop(14, 1.06),
        Expression.stop(16, 1.46),
        Expression.stop(18, 2.02),
        Expression.stop(20, 2.02),
        Expression.stop(22, 2.02),
    )

    fun remove(style: Style) {
        layersReady = false
        runCatching { style.removeLayer(VEHICLE_LAYER) }
        runCatching { style.removeSource(VEHICLE_SOURCE) }
        runCatching { style.removeImage(NavigationVehicleIcon.styleImageId()) }
    }

    fun sync(style: Style, lat: Double, lng: Double, bearingDegrees: Double) {
        try {
            registerIcon(style)
            val feature = Feature.fromGeometry(Point.fromLngLat(lng, lat))
            val anchor = routeAnchorLayerId(style)

            if (!layersReady) {
                remove(style)
                style.addSource(GeoJsonSource(VEHICLE_SOURCE, feature))
                val vehicleLayer = SymbolLayer(VEHICLE_LAYER, VEHICLE_SOURCE).withProperties(
                    PropertyFactory.iconImage(NavigationVehicleIcon.styleImageId()),
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
                PropertyFactory.iconRotate(bearingDegrees.toFloat()),
            )
        } catch (e: Exception) {
            Log.e(TAG, "sync vehicle failed", e)
            layersReady = false
        }
    }

    private fun registerIcon(style: Style) {
        val imageId = NavigationVehicleIcon.styleImageId()
        runCatching { style.removeImage(imageId) }
        try {
            style.addImage(imageId, NavigationVehicleIcon.createBitmap(160))
        } catch (_: Exception) {
            style.addImage(imageId, NavigationVehicleIcon.createBitmap(160), true)
        }
    }

    private fun routeAnchorLayerId(style: Style): String? {
        val candidates = listOf(
            "roadguide_directions_route_line",
            AppMapStyle.BUILDING_3D_LAYER_ID,
            AppMapStyle.BUILDING_LAYER_ID,
        )
        return candidates.firstOrNull { style.getLayer(it) != null }
    }

    private fun addAbove(style: Style, layer: org.maplibre.android.style.layers.Layer, aboveId: String?) {
        if (aboveId != null && style.getLayer(aboveId) != null) {
            style.addLayerAbove(layer, aboveId)
        } else {
            style.addLayer(layer)
        }
    }
}
