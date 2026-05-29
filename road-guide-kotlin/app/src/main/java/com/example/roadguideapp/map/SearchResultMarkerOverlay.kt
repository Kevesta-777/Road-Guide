package com.example.roadguideapp.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/**
 * Screen-space pin for the active Pelias search selection. The pin tip anchors on the result
 * coordinate and stays a fixed size in dp while panning or zooming (same approach as directions
 * waypoint markers).
 */
@Composable
internal fun SearchResultMarkerOverlay(
    location: LatLng?,
    map: MapLibreMap?,
    cameraTick: Int,
    modifier: Modifier = Modifier,
) {
    if (location == null || map == null) return

    val density = LocalDensity.current
    val headRadiusPx = with(density) { 9.dp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        cameraTick

        val screen = map.projection.toScreenLocation(location)
        drawSearchPin(
            anchor = Offset(screen.x, screen.y),
            headRadiusPx = headRadiusPx,
            strokePx = strokePx,
        )
    }
}

private val PinFill = Color(0xFFFF3B30)
private val PinStroke = Color.White
private val PinHighlight = Color.White

private fun DrawScope.drawSearchPin(
    anchor: Offset,
    headRadiusPx: Float,
    strokePx: Float,
) {
    val tip = anchor
    val r = headRadiusPx
    val headCenterY = tip.y - r * 2.15f
    val cx = tip.x
    val cy = headCenterY

    val balloon = Path().apply {
        moveTo(cx, tip.y)
        quadraticTo(cx - r * 1.25f, tip.y - r * 1.15f, cx - r, cy)
        arcTo(
            rect = Rect(cx - r, cy - r, cx + r, cy + r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false,
        )
        quadraticTo(cx + r * 1.25f, tip.y - r * 1.15f, cx, tip.y)
        close()
    }

    drawPath(balloon, PinFill)
    drawPath(balloon, PinStroke, style = Stroke(width = strokePx))
    drawCircle(
        color = PinHighlight,
        radius = r * 0.3f,
        center = Offset(cx, cy),
    )
}
