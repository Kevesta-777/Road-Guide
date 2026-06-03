package com.example.roadguideapp.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.maplibre.android.maps.MapLibreMap

/**
 * Screen-space waypoint markers for directions (Apple Maps–style). Sizes are in dp and do not
 * change when the user zooms or tilts the map.
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
    val density = LocalDensity.current
    val endpointRadiusPx = with(density) { 7.dp.toPx() }
    val endpointStrokePx = with(density) { 3.dp.toPx() }
    val viaRadiusPx = with(density) { 11.dp.toPx() }
    val viaStrokePx = with(density) { 2.dp.toPx() }
    val labelTextPx = with(density) { 12.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        cameraTick

        ordered.forEachIndexed { idx, place ->
            val screen = map.projection.toScreenLocation(place.latLng)
            val center = Offset(screen.x, screen.y)
            when {
                idx == 0 -> drawEndpointMarker(
                    center = center,
                    fill = OriginGreen,
                    radiusPx = endpointRadiusPx,
                    strokePx = endpointStrokePx,
                )
                idx == lastIndex && lastIndex > 0 -> drawEndpointMarker(
                    center = center,
                    fill = DestRed,
                    radiusPx = endpointRadiusPx,
                    strokePx = endpointStrokePx,
                )
                else -> drawViaMarker(
                    center = center,
                    label = idx.toString(),
                    radiusPx = viaRadiusPx,
                    strokePx = viaStrokePx,
                    labelTextPx = labelTextPx,
                )
            }
        }
    }
}

private val OriginGreen = Color(0xFF34C759)
private val DestRed = Color(0xFFFF3B30)
private val ViaRing = Color(0xFF8E8E93)
private val ViaLabel = Color(0xFF1C1C1E)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEndpointMarker(
    center: Offset,
    fill: Color,
    radiusPx: Float,
    strokePx: Float,
) {
    drawCircle(color = fill, radius = radiusPx, center = center)
    drawCircle(
        color = Color.White,
        radius = radiusPx,
        center = center,
        style = Stroke(width = strokePx),
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
