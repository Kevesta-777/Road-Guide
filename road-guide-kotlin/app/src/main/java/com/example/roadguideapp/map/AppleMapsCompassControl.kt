package com.example.roadguideapp.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun AppleMapsCompassControl(
    sheetTheme: AppleMapsSheetTheme,
    mapBearingDegrees: Float,
    onClick: () -> Unit,
    onBearingDragDegrees: (Float) -> Unit,
    modifier: Modifier = Modifier,
    compassContentDescription: String,
) {
    val textMeasurer = rememberTextMeasurer()
    val normalizedBearing = ((mapBearingDegrees % 360f) + 360f) % 360f
    val onClickState by rememberUpdatedState(onClick)
    val onBearingState by rememberUpdatedState(onBearingDragDegrees)
    val bearingState by rememberUpdatedState(mapBearingDegrees)

    val dialSurface = sheetTheme.compassDialSurface
    val dialRing = sheetTheme.compassDialRing
    val tickColor = sheetTheme.compassTick
    val labelColor = sheetTheme.compassCardinalText

    Surface(
        modifier = modifier.size(AppleMapsUiTokens.CompassSizeDp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = dialSurface,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .size(AppleMapsUiTokens.CompassSizeDp)
                .semantics { contentDescription = compassContentDescription }
                .pointerInput(Unit) {
                    val touchSlop = viewConfiguration.touchSlop.toFloat()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointerId = down.id
                        val start = down.position
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val minRadiusPx = minOf(size.width, size.height) * 0.12f
                        var dragged = false
                        var refAngle = 0f
                        var refBearing = 0f
                        try {
                            drag(pointerId) { change ->
                                val pos = change.position
                                if (!dragged) {
                                    val travel = hypot(pos.x - start.x, pos.y - start.y)
                                    if (travel < touchSlop) return@drag
                                    dragged = true
                                    val ringPos = ringTouch(center, pos, minRadiusPx)
                                    refAngle = clockwiseFromNorthDegrees(center, ringPos)
                                    refBearing = bearingState
                                }
                                val ringPos = ringTouch(center, pos, minRadiusPx)
                                val angle = clockwiseFromNorthDegrees(center, ringPos)
                                val delta = shortestAngleDeltaDegrees(refAngle, angle)
                                onBearingState(normalizeBearing360(refBearing + delta))
                                change.consume()
                            }
                        } finally {
                            if (!dragged) onClickState()
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.minDimension * 0.44f
                val tickOuter = outerRadius * 0.82f
                val tickShortInner = outerRadius * 0.68f
                val tickLongInner = outerRadius * 0.58f

                drawCircle(
                    color = dialRing,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = size.minDimension * 0.012f),
                )

                repeat(32) { index ->
                    val angleDegrees = index * 360f / 32f
                    val radians = Math.toRadians((angleDegrees - 90f).toDouble())
                    val isCardinalTick = index % 8 == 0
                    val inner = if (isCardinalTick) tickLongInner else tickShortInner
                    val start = Offset(
                        x = center.x + cos(radians).toFloat() * inner,
                        y = center.y + sin(radians).toFloat() * inner,
                    )
                    val end = Offset(
                        x = center.x + cos(radians).toFloat() * tickOuter,
                        y = center.y + sin(radians).toFloat() * tickOuter,
                    )
                    drawLine(
                        color = tickColor,
                        start = start,
                        end = end,
                        strokeWidth = if (isCardinalTick) 1.4f else 0.9f,
                        cap = StrokeCap.Round,
                    )
                }

                val pointerAngle = Math.toRadians((normalizedBearing - 90f).toDouble())
                val pointerTip = Offset(
                    x = center.x + cos(pointerAngle).toFloat() * outerRadius * 0.9f,
                    y = center.y + sin(pointerAngle).toFloat() * outerRadius * 0.9f,
                )
                val pointerBaseCenter = Offset(
                    x = center.x + cos(pointerAngle).toFloat() * outerRadius * 0.56f,
                    y = center.y + sin(pointerAngle).toFloat() * outerRadius * 0.56f,
                )
                val perpendicular = pointerAngle + Math.PI / 2.0
                val halfBase = size.minDimension * 0.055f
                val pointer = Path().apply {
                    moveTo(pointerTip.x, pointerTip.y)
                    lineTo(
                        pointerBaseCenter.x + cos(perpendicular).toFloat() * halfBase,
                        pointerBaseCenter.y + sin(perpendicular).toFloat() * halfBase,
                    )
                    lineTo(
                        pointerBaseCenter.x - cos(perpendicular).toFloat() * halfBase,
                        pointerBaseCenter.y - sin(perpendicular).toFloat() * halfBase,
                    )
                    close()
                }
                drawPath(pointer, Color(0xFFFF3B30))

                val label = cardinalLabel(normalizedBearing)
                val centerLabel = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                drawText(
                    textLayoutResult = centerLabel,
                    topLeft = Offset(
                        x = center.x - centerLabel.size.width / 2f,
                        y = center.y - centerLabel.size.height / 2f,
                    ),
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun shouldShowAppleMapsCompass(
    mapBearingDegrees: Float,
    is3d: Boolean,
): Boolean {
    return true
}

private fun cardinalLabel(normalizedBearing: Float): String {
    return when (((normalizedBearing / 90f).roundToInt() % 4 + 4) % 4) {
        0 -> "N"
        1 -> "E"
        2 -> "S"
        else -> "W"
    }
}

/** Clockwise degrees from north, matching MapLibre camera bearing (0 = north up on screen). */
private fun clockwiseFromNorthDegrees(center: Offset, touch: Offset): Float {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val deg = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), (-dy).toDouble())).toFloat()
    return normalizeBearing360(deg)
}

private fun ringTouch(center: Offset, touch: Offset, minRadiusPx: Float): Offset {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val dist = hypot(dx, dy)
    if (dist < 1e-3f) {
        return Offset(center.x, center.y - minRadiusPx)
    }
    val r = max(dist, minRadiusPx)
    return Offset(center.x + dx / dist * r, center.y + dy / dist * r)
}

private fun shortestAngleDeltaDegrees(fromDeg: Float, toDeg: Float): Float {
    var d = (toDeg - fromDeg) % 360f
    if (d > 180f) d -= 360f
    if (d < -180f) d += 360f
    return d
}

private fun normalizeBearing360(deg: Float): Float {
    var d = deg % 360f
    if (d < 0f) d += 360f
    return d
}
