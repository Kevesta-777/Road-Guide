package com.example.roadguideapp.map

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

internal data class MapScaleRulerState(
    val label: String,
    val widthPx: Float,
)

internal object MapScaleRulerCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun calculate(
        map: MapLibreMap,
        mapView: MapView,
        targetWidthPx: Float,
    ): MapScaleRulerState? {
        val viewWidth = mapView.width
        val viewHeight = mapView.height
        if (viewWidth <= 0 || viewHeight <= 0 || targetWidthPx <= 0f) return null

        val sampleWidthPx = targetWidthPx.coerceAtMost(viewWidth * 0.55f)
        val y = viewHeight * 0.5f
        val centerX = viewWidth * 0.5f
        val half = sampleWidthPx * 0.5f

        val left = runCatching {
            map.projection.fromScreenLocation(PointF(centerX - half, y))
        }.getOrNull() ?: return null
        val right = runCatching {
            map.projection.fromScreenLocation(PointF(centerX + half, y))
        }.getOrNull() ?: return null

        val sampleMeters = haversineMeters(left, right)
        if (sampleMeters <= 0.0 || !sampleMeters.isFinite()) return null

        val rulerMeters = niceDistanceAtMost(sampleMeters)
        val widthPx = (sampleWidthPx * rulerMeters / sampleMeters).toFloat()
        return MapScaleRulerState(
            label = formatDistance(rulerMeters),
            widthPx = widthPx,
        )
    }

    private fun niceDistanceAtMost(meters: Double): Double {
        val power = 10.0.pow(floor(log10(meters)))
        var best = power
        for (base in doubleArrayOf(1.0, 2.0, 5.0)) {
            val candidate = base * power
            if (candidate <= meters) best = candidate
        }
        return best
    }

    private fun formatDistance(meters: Double): String {
        return when {
            meters >= 1_000.0 -> {
                val km = meters / 1_000.0
                if (km >= 10.0 || km % 1.0 == 0.0) {
                    String.format(Locale.US, "%.0f km", km)
                } else {
                    String.format(Locale.US, "%.1f km", km)
                }
            }
            meters >= 1.0 -> String.format(Locale.US, "%.0f m", meters)
            else -> String.format(Locale.US, "%.0f cm", meters * 100.0)
        }
    }

    private fun haversineMeters(a: LatLng, b: LatLng): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2.0).pow(2.0) +
            cos(lat1) * cos(lat2) * sin(dLng / 2.0).pow(2.0)
        return 2.0 * EARTH_RADIUS_METERS * asin(sqrt(h))
    }

}

@Composable
internal fun MapScaleRuler(
    state: MapScaleRulerState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val rulerWidth = with(density) { state.widthPx.toDp() }
    val rulerColor = Color.Black

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(18.dp),
    ) {
        Canvas(
            modifier = Modifier
                .width(rulerWidth)
                .height(20.dp),
        ) {
            val strokeWidth = 2.dp.toPx()
            val y = size.height - strokeWidth / 2f
            drawLine(
                color = rulerColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth,
            )
        }
        Text(
            text = state.label,
            color = rulerColor,
            fontSize = 12.sp,
            lineHeight = 9.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
