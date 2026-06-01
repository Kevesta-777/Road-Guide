package com.example.roadguideapp.map

import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point as GeoPoint

/**
 * Directions route on the map (Apple Maps–style): thin blue line with white casing,
 * green origin dot, red destination dot, and numbered via-stop badges (see
 * [DirectionsWaypointMarkersOverlay]).
 */
internal object DirectionsRouteOverlay {

    private const val LINE_SOURCE = "roadguide_directions_route_line_src"
    private const val LINE_CASING_LAYER = "roadguide_directions_route_casing"
    private const val LINE_LAYER = "roadguide_directions_route_line"
    private const val POINTS_SOURCE = "roadguide_directions_route_pts_src"
    private const val LABEL_LAYER = "roadguide_directions_leg_labels"

    /** Legacy single-source id (pre split line/points). */
    private const val LEGACY_SOURCE_ID = "roadguide_directions_route"

    private const val ROUTE_BLUE = "#007AFF"
    private const val CASING_WHITE = "#FFFFFF"
    /** ~80 km/h straight-line ETA when Valhalla is unavailable. */
    private const val ASSUMED_DRIVE_MPS = 22.22

    fun remove(style: Style) {
        runCatching { style.removeLayer(LABEL_LAYER) }
        runCatching { style.removeLayer(LINE_LAYER) }
        // Legacy waypoint layers (markers are drawn in [DirectionsWaypointMarkersOverlay]).
        runCatching { style.removeLayer("roadguide_directions_waypoints") }
        runCatching { style.removeLayer("roadguide_directions_waypoint_numbers") }
        runCatching { style.removeLayer(LINE_CASING_LAYER) }
        runCatching { style.removeSource(POINTS_SOURCE) }
        runCatching { style.removeSource(LINE_SOURCE) }
        runCatching { style.removeSource(LEGACY_SOURCE_ID) }
    }

    fun sync(
        style: Style,
        origin: MapPlaceDetail,
        stops: List<MapPlaceDetail>,
        valhallaRoute: DirectionsRouteResult? = null,
        revealProgress: Float = 1f,
    ) {
        val ordered = listOf(origin) + stops
        val waypoints = ordered.map { it.latLng }
        if (waypoints.size < 2) {
            remove(style)
            return
        }

        val fullLineLatLngs = valhallaRoute?.geometry?.takeIf { it.size >= 2 }
            ?: DirectionsPathOptimizer.buildPolyline(waypoints, segmentsPerLeg = 26)
        val lineLatLngs = DirectionsRouteGeometry.slicePolylineByProgress(
            fullLineLatLngs,
            revealProgress.coerceIn(0f, 1f),
        )
        val linePoints = lineLatLngs.map { GeoPoint.fromLngLat(it.longitude, it.latitude) }
        val lineFeature = Feature.fromGeometry(LineString.fromLngLats(linePoints))

        val pointFeatures = ArrayList<Feature>(waypoints.lastIndex)
        val showLegLabels = revealProgress >= 0.98f

        val valhallaLegs = valhallaRoute?.legs.orEmpty()
        if (showLegLabels) for (i in 0 until waypoints.lastIndex) {
            val leg = valhallaLegs.getOrNull(i)
            val mid = leg?.midPoint ?: run {
                val a = waypoints[i]
                val b = waypoints[i + 1]
                DirectionsPathOptimizer.greatCircleMidpoint(a, b)
            }
            val sec = leg?.durationSeconds?.toLong()
                ?: run {
                    val a = waypoints[i]
                    val b = waypoints[i + 1]
                    (DirectionsPathOptimizer.haversineMeters(a, b) / ASSUMED_DRIVE_MPS)
                        .toLong()
                        .coerceAtLeast(30L)
                }
            val lf = Feature.fromGeometry(GeoPoint.fromLngLat(mid.longitude, mid.latitude))
            lf.addStringProperty("label", formatLegEta(sec))
            pointFeatures.add(lf)
        }

        val pointsCollection = FeatureCollection.fromFeatures(pointFeatures)

        val lineSrc = style.getSource(LINE_SOURCE) as? GeoJsonSource
        val ptSrc = style.getSource(POINTS_SOURCE) as? GeoJsonSource
        if (lineSrc != null && ptSrc != null) {
            lineSrc.setGeoJson(lineFeature)
            ptSrc.setGeoJson(pointsCollection)
            return
        }

        remove(style)

        val lineSource = GeoJsonSource(LINE_SOURCE, lineFeature)
        val pointsSource = GeoJsonSource(POINTS_SOURCE, pointsCollection)

        val casingLayer = LineLayer(LINE_CASING_LAYER, LINE_SOURCE).withProperties(
            PropertyFactory.lineColor(CASING_WHITE),
            PropertyFactory.lineWidth(7.5f),
            PropertyFactory.lineOpacity(0.95f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )

        val lineLayer = LineLayer(LINE_LAYER, LINE_SOURCE).withProperties(
            PropertyFactory.lineColor(ROUTE_BLUE),
            PropertyFactory.lineWidth(4.5f),
            PropertyFactory.lineOpacity(1f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )

        val labelLayer = SymbolLayer(LABEL_LAYER, POINTS_SOURCE)
            .withProperties(
                PropertyFactory.textField(Expression.get("label")),
                PropertyFactory.textSize(11f),
                PropertyFactory.textColor(CASING_WHITE),
                PropertyFactory.textHaloColor(ROUTE_BLUE),
                PropertyFactory.textHaloWidth(1.5f),
                PropertyFactory.textHaloBlur(0.2f),
                PropertyFactory.textAllowOverlap(true),
                PropertyFactory.textIgnorePlacement(true),
            )
            .withFilter(Expression.has("label"))

        val anchorLayerId = routeAnchorLayerId(style)
        try {
            style.addSource(lineSource)
            style.addSource(pointsSource)
            addRouteLayer(style, casingLayer, anchorLayerId)
            addRouteLayer(style, lineLayer, LINE_CASING_LAYER)
            addRouteLayer(style, labelLayer, LINE_LAYER)
        } catch (_: Exception) {
            runCatching {
                style.addSource(lineSource)
                style.addSource(pointsSource)
                style.addLayer(casingLayer)
                style.addLayer(lineLayer)
                style.addLayer(labelLayer)
            }
        }
    }

    private fun routeAnchorLayerId(style: Style): String? {
        val candidates = listOf(
            AppMapStyle.BUILDING_3D_LAYER_ID,
            AppMapStyle.BUILDING_LAYER_ID,
        )
        return candidates.firstOrNull { style.getLayer(it) != null }
    }

    private fun addRouteLayer(style: Style, layer: LineLayer, aboveLayerId: String?) {
        if (aboveLayerId != null && style.getLayer(aboveLayerId) != null) {
            style.addLayerAbove(layer, aboveLayerId)
        } else {
            style.addLayer(layer)
        }
    }

    private fun addRouteLayer(style: Style, layer: SymbolLayer, aboveLayerId: String?) {
        if (aboveLayerId != null && style.getLayer(aboveLayerId) != null) {
            style.addLayerAbove(layer, aboveLayerId)
        } else {
            style.addLayer(layer)
        }
    }

    private fun formatLegEta(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            m > 0 -> "${m}m"
            else -> "<1m"
        }
    }
}
