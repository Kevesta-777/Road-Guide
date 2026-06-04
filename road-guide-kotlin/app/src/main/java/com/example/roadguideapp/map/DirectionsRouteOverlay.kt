package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
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
 * Directions route on the map (Apple Maps–style): line style depends on travel mode
 * (solid drive, dashed bike, dotted walk), with leg ETA labels when the full route is shown.
 */
internal object DirectionsRouteOverlay {

    private const val LINE_SOURCE = "roadguide_directions_route_line_src"
    private const val LINE_CASING_LAYER = "roadguide_directions_route_casing"
    private const val LINE_LAYER = "roadguide_directions_route_line"
    private const val POINTS_SOURCE = "roadguide_directions_route_pts_src"
    private const val LABEL_LAYER = "roadguide_directions_leg_labels"
    private const val DOT_SOURCE = "roadguide_directions_route_dots_src"
    private const val DOT_CASING_LAYER = "roadguide_directions_route_dots_casing"
    private const val DOT_LAYER = "roadguide_directions_route_dots"

    /** Legacy single-source id (pre split line/points). */
    private const val LEGACY_SOURCE_ID = "roadguide_directions_route"

    /** ~80 km/h straight-line ETA when Valhalla is unavailable. */
    private const val ASSUMED_DRIVE_MPS = 22.22

    private val BICYCLE_DASH = arrayOf(1.8f, 1.35f)
    private val SOLID_DASH = arrayOf<Float>()

    fun remove(style: Style) {
        runCatching { style.removeLayer(LABEL_LAYER) }
        runCatching { style.removeLayer(DOT_LAYER) }
        runCatching { style.removeLayer(DOT_CASING_LAYER) }
        runCatching { style.removeLayer(LINE_LAYER) }
        runCatching { style.removeLayer("roadguide_directions_waypoints") }
        runCatching { style.removeLayer("roadguide_directions_waypoint_numbers") }
        runCatching { style.removeLayer(LINE_CASING_LAYER) }
        runCatching { style.removeSource(DOT_SOURCE) }
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
        travelMode: DirectionsTravelMode = DirectionsTravelMode.Drive,
        isDarkAppearance: Boolean = false,
    ) {
        val palette = DirectionsRouteStyle.palette(isDarkAppearance)
        val navigationMode = showRemainingRoute
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
        if (lineLatLngs.size < 2 && travelMode != DirectionsTravelMode.Walk) {
            remove(style)
            return
        }
        if (lineLatLngs.isEmpty() && travelMode == DirectionsTravelMode.Walk) {
            remove(style)
            return
        }

        val linePoints = lineLatLngs.map { GeoPoint.fromLngLat(it.longitude, it.latitude) }
        val lineFeature = if (linePoints.size >= 2) {
            Feature.fromGeometry(LineString.fromLngLats(linePoints))
        } else {
            null
        }

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

        val dotSpacingM = if (showRemainingRoute) 9.0 else 11.0
        val dotLatLngs = if (travelMode == DirectionsTravelMode.Walk) {
            DirectionsRouteGeometry.samplePointsAlongPolyline(lineLatLngs, dotSpacingM)
        } else {
            emptyList()
        }
        val dotFeatures = dotLatLngs.map { pt ->
            Feature.fromGeometry(GeoPoint.fromLngLat(pt.longitude, pt.latitude))
        }
        val dotsCollection = FeatureCollection.fromFeatures(dotFeatures)

        val lineSrc = style.getSource(LINE_SOURCE) as? GeoJsonSource
        val ptSrc = style.getSource(POINTS_SOURCE) as? GeoJsonSource
        val dotSrc = style.getSource(DOT_SOURCE) as? GeoJsonSource
        val needsLineLayer = travelMode != DirectionsTravelMode.Walk && lineFeature != null
        val canUpdateInPlace = ptSrc != null &&
            style.getLayer(LABEL_LAYER) != null &&
            (!needsLineLayer || (lineSrc != null && style.getLayer(LINE_LAYER) != null))
        if (canUpdateInPlace) {
            if (lineSrc != null && lineFeature != null) {
                lineSrc.setGeoJson(lineFeature)
            }
            ptSrc.setGeoJson(pointsCollection)
            if (dotSrc != null) {
                dotSrc.setGeoJson(dotsCollection)
            } else if (travelMode == DirectionsTravelMode.Walk) {
                ensureDotLayers(style, palette, dotsCollection, navigationMode)
            }
            applyRouteStyle(style, palette, navigationMode, travelMode)
            return
        }

        remove(style)

        if (lineFeature != null) {
            val lineSource = GeoJsonSource(LINE_SOURCE, lineFeature)
            style.addSource(lineSource)
        }
        val pointsSource = GeoJsonSource(POINTS_SOURCE, pointsCollection)
        style.addSource(pointsSource)

        val casingLayer = LineLayer(LINE_CASING_LAYER, LINE_SOURCE).withProperties(
            *lineCasingProperties(palette, navigationMode, travelMode),
        )

        val lineLayer = LineLayer(LINE_LAYER, LINE_SOURCE).withProperties(
            *lineFillProperties(palette, navigationMode, travelMode),
        )

        val labelLayer = SymbolLayer(LABEL_LAYER, POINTS_SOURCE)
            .withProperties(
                PropertyFactory.textField(Expression.get("label")),
                PropertyFactory.textSize(12f),
                PropertyFactory.textColor(palette.legLabelText),
                PropertyFactory.textHaloColor(
                    DirectionsRouteStyle.previewFillColor(palette, travelMode),
                ),
                PropertyFactory.textHaloWidth(1.5f),
                PropertyFactory.textHaloBlur(0.2f),
                PropertyFactory.textAllowOverlap(true),
                PropertyFactory.textIgnorePlacement(true),
            )
            .withFilter(Expression.has("label"))

        val anchorLayerId = routeAnchorLayerId(style)
        try {
            if (lineFeature != null) {
                addRouteLayer(style, casingLayer, anchorLayerId)
                addRouteLayer(style, lineLayer, LINE_CASING_LAYER)
            }
            addRouteLayer(style, labelLayer, lineLayerOrAnchor(style, anchorLayerId))
            if (travelMode == DirectionsTravelMode.Walk) {
                ensureDotLayers(style, palette, dotsCollection, navigationMode, aboveLayerId = LABEL_LAYER)
            }
        } catch (_: Exception) {
            runCatching {
                if (lineFeature != null) {
                    style.addLayer(casingLayer)
                    style.addLayer(lineLayer)
                }
                style.addLayer(labelLayer)
                if (travelMode == DirectionsTravelMode.Walk) {
                    ensureDotLayers(style, palette, dotsCollection, navigationMode)
                }
            }
        }
        applyRouteStyle(style, palette, navigationMode, travelMode)
    }

    private fun lineLayerOrAnchor(style: Style, anchorLayerId: String?): String? {
        return when {
            style.getLayer(LINE_LAYER) != null -> LINE_LAYER
            anchorLayerId != null -> anchorLayerId
            else -> null
        }
    }

    private fun ensureDotLayers(
        style: Style,
        palette: DirectionsRoutePalette,
        dotsCollection: FeatureCollection,
        navigationMode: Boolean,
        aboveLayerId: String? = null,
    ) {
        if (style.getSource(DOT_SOURCE) == null) {
            style.addSource(GeoJsonSource(DOT_SOURCE, dotsCollection))
        } else {
            (style.getSource(DOT_SOURCE) as? GeoJsonSource)?.setGeoJson(dotsCollection)
        }
        if (style.getLayer(DOT_LAYER) != null) return
        val dotCasing = CircleLayer(DOT_CASING_LAYER, DOT_SOURCE).withProperties(
            PropertyFactory.circleColor(palette.walkDotCasing),
            PropertyFactory.circleRadius(DirectionsRouteStyle.dotCasingRadiusExpression(navigationMode)),
            PropertyFactory.circleOpacity(0.98f),
            PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
        )
        val dotLayer = CircleLayer(DOT_LAYER, DOT_SOURCE).withProperties(
            PropertyFactory.circleColor(DirectionsRouteStyle.previewFillColor(palette, DirectionsTravelMode.Walk)),
            PropertyFactory.circleRadius(DirectionsRouteStyle.dotRadiusExpression(navigationMode)),
            PropertyFactory.circleOpacity(1f),
            PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
        )
        val anchor = aboveLayerId?.takeIf { style.getLayer(it) != null }
            ?: style.getLayer(LINE_LAYER)?.id
            ?: routeAnchorLayerId(style)
        if (anchor != null && style.getLayer(anchor) != null) {
            style.addLayerAbove(dotCasing, anchor)
            style.addLayerAbove(dotLayer, DOT_CASING_LAYER)
        } else {
            style.addLayer(dotCasing)
            style.addLayer(dotLayer)
        }
    }

    /** Topmost base-map layer so the route draws above POI/symbol glyphs. */
    private fun routeAnchorLayerId(style: Style): String? =
        style.layers.asReversed().firstOrNull { !it.id.startsWith("roadguide_") }?.id

    private fun applyRouteStyle(
        style: Style,
        palette: DirectionsRoutePalette,
        navigationMode: Boolean,
        travelMode: DirectionsTravelMode,
    ) {
        val useWalkDots = travelMode == DirectionsTravelMode.Walk
        val lineVisibility = if (useWalkDots) Property.NONE else Property.VISIBLE
        (style.getLayer(LINE_CASING_LAYER) as? LineLayer)?.setProperties(
            *lineCasingProperties(palette, navigationMode, travelMode),
            PropertyFactory.visibility(lineVisibility),
        )
        (style.getLayer(LINE_LAYER) as? LineLayer)?.setProperties(
            *lineFillProperties(palette, navigationMode, travelMode),
            PropertyFactory.visibility(lineVisibility),
        )
        val dotVisibility = if (useWalkDots) Property.VISIBLE else Property.NONE
        (style.getLayer(DOT_CASING_LAYER) as? CircleLayer)?.setProperties(
            PropertyFactory.visibility(dotVisibility),
            PropertyFactory.circleColor(palette.walkDotCasing),
            PropertyFactory.circleRadius(DirectionsRouteStyle.dotCasingRadiusExpression(navigationMode)),
        )
        (style.getLayer(DOT_LAYER) as? CircleLayer)?.setProperties(
            PropertyFactory.visibility(dotVisibility),
            PropertyFactory.circleColor(
                DirectionsRouteStyle.routeFillColor(palette, navigationMode, DirectionsTravelMode.Walk),
            ),
            PropertyFactory.circleRadius(DirectionsRouteStyle.dotRadiusExpression(navigationMode)),
        )
        (style.getLayer(LABEL_LAYER) as? SymbolLayer)?.setProperties(
            PropertyFactory.textColor(palette.legLabelText),
            PropertyFactory.textHaloColor(DirectionsRouteStyle.previewFillColor(palette, travelMode)),
        )
    }

    private fun lineCasingProperties(
        palette: DirectionsRoutePalette,
        navigationMode: Boolean,
        travelMode: DirectionsTravelMode,
    ): Array<org.maplibre.android.style.layers.PropertyValue<*>> {
        return arrayOf(
            PropertyFactory.lineColor(
                if (navigationMode) {
                    DirectionsRouteStyle.navBorderColor(palette, travelMode)
                } else {
                    palette.previewCasing
                },
            ),
            PropertyFactory.lineWidth(DirectionsRouteStyle.routeCasingWidthExpression(navigationMode)),
            PropertyFactory.lineOpacity(if (navigationMode) 0.98f else 0.96f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineDasharray(lineDasharrayForMode(travelMode)),
        )
    }

    private fun lineFillProperties(
        palette: DirectionsRoutePalette,
        navigationMode: Boolean,
        travelMode: DirectionsTravelMode,
    ): Array<org.maplibre.android.style.layers.PropertyValue<*>> {
        return arrayOf(
            PropertyFactory.lineColor(
                DirectionsRouteStyle.routeFillColor(palette, navigationMode, travelMode),
            ),
            PropertyFactory.lineWidth(DirectionsRouteStyle.routeLineWidthExpression(navigationMode)),
            PropertyFactory.lineOpacity(if (navigationMode) 0.92f else 1f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineDasharray(lineDasharrayForMode(travelMode)),
        )
    }

    private fun lineDasharrayForMode(travelMode: DirectionsTravelMode): Array<Float> = when (travelMode) {
        DirectionsTravelMode.Bicycle -> BICYCLE_DASH
        DirectionsTravelMode.Walk -> SOLID_DASH
        DirectionsTravelMode.Drive -> SOLID_DASH
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
