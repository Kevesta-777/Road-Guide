package com.example.roadguideapp.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.maplibre.android.maps.MapLibreMap

/**
 * Screen-space waypoint markers for directions. Endpoint pins use fixed dp size and anchor
 * their tip on the route coordinate while panning or zooming.
 */
@Composable
internal fun DirectionsWaypointMarkersOverlay(
    directions: AppleMapSheet.Directions?,
    map: MapLibreMap?,
    cameraTick: Int,
    modifier: Modifier = Modifier,
) {
    if (directions == null || map == null) return

    val ordered = remember(directions.tripWaypoints.map { it.id }) {
        directions.tripWaypoints
    }
    val lastIndex = ordered.lastIndex
    val context = LocalContext.current
    val density = LocalDensity.current
    val markerHeightPx = with(density) { RouteWaypointMarkerBitmaps.MARKER_HEIGHT_DP.dp.toPx() }
    val originPin = remember { RouteWaypointMarkerBitmaps.originPin(context) }
    val destinationPin = remember { RouteWaypointMarkerBitmaps.destinationPin(context) }
    val viaRadiusPx = with(density) { 11.dp.toPx() }
    val viaStrokePx = with(density) { 2.dp.toPx() }
    val labelTextPx = with(density) { 12.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        cameraTick

        ordered.forEachIndexed { idx, place ->
            val screen = map.projection.toScreenLocation(place.latLng)
            val anchor = Offset(screen.x, screen.y)
            when {
                idx == 0 -> drawRouteEndpointPin(
                    anchor = anchor,
                    image = originPin,
                    heightPx = markerHeightPx,
                )
                idx == lastIndex && lastIndex > 0 -> drawRouteEndpointPin(
                    anchor = anchor,
                    image = destinationPin,
                    heightPx = markerHeightPx,
                )
                else -> drawViaMarker(
                    center = anchor,
                    label = idx.toString(),
                    radiusPx = viaRadiusPx,
                    strokePx = viaStrokePx,
                    labelTextPx = labelTextPx,
                )
            }
        }
    }
}

private val ViaRing = Color(0xFF8E8E93)
private val ViaLabel = Color(0xFF1C1C1E)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRouteEndpointPin(
    anchor: Offset,
    image: ImageBitmap,
    heightPx: Float,
) {
    val aspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
    val widthPx = heightPx * aspect
    val widthInt = widthPx.toInt().coerceAtLeast(1)
    val heightInt = heightPx.toInt().coerceAtLeast(1)
    drawImage(
        image = image,
        dstOffset = IntOffset(
            x = (anchor.x - widthPx / 2f).toInt(),
            y = (anchor.y - heightPx).toInt(),
        ),
        dstSize = IntSize(widthInt, heightInt),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawViaMarker(
    center: Offset,
    label: String,
    radiusPx: Float,
    strokePx: Float,
    labelTextPx: Float,
) {
    drawCircle(color = Color.White, radius = radiusPx, center = center)
    drawCircle(
        color = ViaRing,
        radius = radiusPx,
        center = center,
        style = Stroke(width = strokePx),
    )
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(
            (ViaLabel.red * 255).toInt(),
            (ViaLabel.green * 255).toInt(),
            (ViaLabel.blue * 255).toInt(),
        )
        textSize = labelTextPx
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textY = center.y - (paint.descent() + paint.ascent()) / 2f
    drawContext.canvas.nativeCanvas.drawText(label, center.x, textY, paint)
}
