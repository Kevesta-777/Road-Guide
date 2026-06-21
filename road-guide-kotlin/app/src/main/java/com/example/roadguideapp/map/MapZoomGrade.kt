package com.example.roadguideapp.map

import android.graphics.PointF
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.goldhunt.GoldHuntConfig
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

internal data class MapZoomGradeState(
    val zoomText: String,
    val groundSpanText: String,
    val hintText: String?,
)

internal object MapZoomGradeCalculator {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun fromMap(
        map: MapLibreMap,
        mapView: MapView,
        goldHuntActive: Boolean = true,
    ): MapZoomGradeState? {
        val zoom = map.cameraPosition.zoom
        val viewWidth = mapView.width
        val viewHeight = mapView.height
        if (viewWidth <= 0 || viewHeight <= 0) return null

        val sampleWidthPx = (viewWidth * 0.45f).coerceAtMost(viewWidth * 0.55f)
        val y = viewHeight * 0.5f
        val centerX = viewWidth * 0.5f
        val half = sampleWidthPx * 0.5f

        val left = runCatching {
            map.projection.fromScreenLocation(PointF(centerX - half, y))
        }.getOrNull() ?: return null
        val right = runCatching {
            map.projection.fromScreenLocation(PointF(centerX + half, y))
        }.getOrNull() ?: return null

        val spanMeters = haversineMeters(left, right)
        if (spanMeters <= 0.0 || !spanMeters.isFinite()) return null

        val zoomText = String.format(Locale.US, "Zoom %.1f", zoom)
        val groundSpanText = String.format(Locale.US, "≈ %s across", formatDistance(spanMeters))
        val hintText = if (!goldHuntActive) {
            null
        } else {
            when {
                zoom < GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM ->
                    "Treasures from zoom ${formatZoomThreshold(GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM)}+"
                zoom < GoldHuntConfig.SECRET_DISPLAY_MIN_ZOOM ->
                    "Secret places from zoom ${formatZoomThreshold(GoldHuntConfig.SECRET_DISPLAY_MIN_ZOOM)}+"
                else -> "Treasures & secret places visible"
            }
        }

        return MapZoomGradeState(
            zoomText = zoomText,
            groundSpanText = groundSpanText,
            hintText = hintText,
        )
    }

    private fun formatZoomThreshold(zoom: Double): String =
        if (zoom % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", zoom)
        } else {
            String.format(Locale.US, "%.1f", zoom)
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
internal fun MapZoomGradeDisplay(
    state: MapZoomGradeState,
    modifier: Modifier = Modifier,
) {
    val line = buildString {
        append(state.zoomText)
        append(" · ")
        append(
            state.groundSpanText
                .removePrefix("≈ ")
                .substringBefore(" across")
                .trim(),
        )
        state.hintText?.let {
            append(" · ")
            append(it)
        }
    }
    Text(
        text = line,
        color = Color.Black,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        modifier = modifier,
    )
}
