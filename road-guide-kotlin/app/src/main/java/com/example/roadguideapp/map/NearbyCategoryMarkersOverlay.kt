package com.example.roadguideapp.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.maplibre.android.maps.MapLibreMap
import kotlin.math.hypot

/**
 * Screen-space nearby category markers. Size is fixed in dp and does not change when zooming.
 */
@Composable
internal fun NearbyCategoryMarkersOverlay(
    category: AppleNearbyShortcut?,
    results: List<PeliasSearchResult>,
    map: MapLibreMap?,
    cameraTick: Int,
    onMarkerClick: (PeliasSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (category == null || map == null || results.isEmpty()) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val markerSizePx = with(density) { NearbyCategoryMarkerBitmaps.MARKER_DISPLAY_DP.dp.toPx() }
    val half = markerSizePx / 2f
    val markerSizeInt = markerSizePx.toInt().coerceAtLeast(1)
    val hitRadiusPx = half * 1.35f

    val bitmap = remember(category.labelRes, density.density) {
        NearbyCategoryMarkerBitmaps.createMarkerBitmap(
            context = context,
            category = category,
            density = density.density,
        )
    }
    val image = remember(bitmap) { bitmap.asImageBitmap() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(results, category.labelRes, cameraTick, hitRadiusPx) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val hit = hitNearbyMarker(down.position, results, map, hitRadiusPx)
                    if (hit == null) return@awaitEachGesture
                    down.consume()
                    if (waitForUpOrCancellation() != null) {
                        onMarkerClick(hit)
                    }
                }
            },
    ) {
        @Suppress("UNUSED_EXPRESSION")
        cameraTick

        results.forEach { result ->
            val screen = map.projection.toScreenLocation(result.latLng)
            drawImage(
                image = image,
                dstOffset = IntOffset(
                    (screen.x - half).toInt(),
                    (screen.y - half).toInt(),
                ),
                dstSize = IntSize(markerSizeInt, markerSizeInt),
            )
        }
    }
}

private fun hitNearbyMarker(
    tap: Offset,
    results: List<PeliasSearchResult>,
    map: MapLibreMap,
    hitRadiusPx: Float,
): PeliasSearchResult? {
    var best: PeliasSearchResult? = null
    var bestDistance = hitRadiusPx
    for (result in results) {
        val screen = map.projection.toScreenLocation(result.latLng)
        val distance = hypot(
            (tap.x - screen.x).toDouble(),
            (tap.y - screen.y).toDouble(),
        ).toFloat()
        if (distance <= bestDistance) {
            bestDistance = distance
            best = result
        }
    }
    return best
}
