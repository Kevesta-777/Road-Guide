package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
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
    /** Sygic-style navigation route (vibrant fill + dark border). */
    private const val ROUTE_NAV_BLUE = "#2B7CE8"
    private const val ROUTE_NAV_BORDER = "#0F2238"
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
        showRemainingRoute: Boolean = false,
        navigationGeometry: List<LatLng>? = null,
        routeStartPosition: LatLng? = null,
        routeStartDistanceM: Double? = null,
        routeTrimAheadM: Double = 0.0,
    ) {
        val ordered = listOf(origin) + stops
        val waypoints = ordered.map { it.latLng }
        if (waypoints.size < 2) {
            remove(style)
            return
        }

        val rawLineLatLngs = navigationGeometry?.takeIf { it.size >= 2 }
            ?: valhallaRoute?.geometry?.takeIf { it.size >= 2 }
            ?: DirectionsPathOptimizer.buildPolyline(waypoints, segmentsPerLeg = 32)
        val fullLineLatLngs = DirectionsRouteGeometry.prepareForMapDisplay(rawLineLatLngs)
        val lineLatLngs = when {
            showRemainingRoute && routeStartPosition != null && routeStartDistanceM != null -> {
                DirectionsRouteGeometry.sliceRemainingRouteAtMarker(
                    fullLineLatLngs,
                    anchor = routeStartPosition,
                    distanceAlongRouteM = routeStartDistanceM,
                    trimAheadM = routeTrimAheadM,
                )
            }
            showRemainingRoute && routeStartPosition != null -> {
                DirectionsRouteGeometry.slicePolylineFromPosition(
                    fullLineLatLngs,
                    routeStartPosition,
                    trimAheadM = routeTrimAheadM,
                )
            }
            showRemainingRoute -> {
                DirectionsRouteGeometry.slicePolylineRemaining(
                    fullLineLatLngs,
                    revealProgress.coerceIn(0f, 1f),
                )
            }
            else -> {
                DirectionsRouteGeometry.slicePolylineByProgress(
                    fullLineLatLngs,
                    revealProgress.coerceIn(0f, 1f),
                )
            }
        }
        if (lineLatLngs.size < 2) {
            remove(style)
            return
        }
        val linePoints = lineLatLngs.map { GeoPoint.fromLngLat(it.longitude, it.latitude) }
        val lineFeature = Feature.fromGeometry(LineString.fromLngLats(linePoints))

        val pointFeatures = ArrayList<Feature>(waypoints.lastIndex)
        val showLegLabels = !showRemainingRoute && revealProgress >= 0.98f

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
            applyLineStyle(style, showRemainingRoute)
            return
        }

        remove(style)

        val lineSource = GeoJsonSource(LINE_SOURCE, lineFeature)
        val pointsSource = GeoJsonSource(POINTS_SOURCE, pointsCollection)

        val casingLayer = LineLayer(LINE_CASING_LAYER, LINE_SOURCE).withProperties(
            *lineCasingProperties(showRemainingRoute),
        )

        val lineLayer = LineLayer(LINE_LAYER, LINE_SOURCE).withProperties(
            *lineFillProperties(showRemainingRoute),
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
            "transportation",
            PmtilesOverviewStylePatch.overviewLayerId("transportation"),
            AppMapStyle.BUILDING_3D_LAYER_ID,
            AppMapStyle.BUILDING_LAYER_ID,
        )
        return candidates.firstOrNull { style.getLayer(it) != null }
    }

    private fun applyLineStyle(style: Style, navigationMode: Boolean) {
        (style.getLayer(LINE_CASING_LAYER) as? LineLayer)?.setProperties(
            *lineCasingProperties(navigationMode),
        )
        (style.getLayer(LINE_LAYER) as? LineLayer)?.setProperties(
            *lineFillProperties(navigationMode),
        )
    }

    private fun lineCasingProperties(navigationMode: Boolean): Array<org.maplibre.android.style.layers.PropertyValue<*>> {
        val width = if (navigationMode) navigationRouteCasingWidthExpression() else routeCasingWidthExpression()
        return arrayOf(
            PropertyFactory.lineColor(if (navigationMode) ROUTE_NAV_BORDER else CASING_WHITE),
            PropertyFactory.lineWidth(width),
            PropertyFactory.lineOpacity(if (navigationMode) 0.98f else 0.95f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
    }

    private fun lineFillProperties(navigationMode: Boolean): Array<org.maplibre.android.style.layers.PropertyValue<*>> {
        val width = if (navigationMode) navigationRouteLineWidthExpression() else routeLineWidthExpression()
        return arrayOf(
            PropertyFactory.lineColor(if (navigationMode) ROUTE_NAV_BLUE else ROUTE_BLUE),
            PropertyFactory.lineWidth(width),
            PropertyFactory.lineOpacity(if (navigationMode) 0.88f else 1f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
    }

    /** Width tuned to sit inside yellow road fill (OpenMapTiles-style). */
    private fun routeLineWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 1.4f),
        Expression.stop(16, 2.2f),
        Expression.stop(18, 3.2f),
        Expression.stop(20, 3.8f),
        Expression.stop(22, 4f),
    )

    private fun routeCasingWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 2.6f),
        Expression.stop(16, 3.6f),
        Expression.stop(18, 4.8f),
        Expression.stop(20, 5.4f),
        Expression.stop(22, 5.6f),
    )

    /** Navigation route: ~70% of typical road fill width at driving zoom. */
    private fun navigationRouteLineWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 3.2f),
        Expression.stop(16, 4.2f),
        Expression.stop(18, 5.2f),
        Expression.stop(20, 5.8f),
        Expression.stop(22, 6f),
    )

    private fun navigationRouteCasingWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 4.8f),
        Expression.stop(16, 6f),
        Expression.stop(18, 7.2f),
        Expression.stop(20, 7.8f),
        Expression.stop(22, 8f),
    )

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
