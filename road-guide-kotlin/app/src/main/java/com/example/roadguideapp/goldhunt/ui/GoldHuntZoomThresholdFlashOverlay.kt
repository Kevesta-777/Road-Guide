package com.example.roadguideapp.goldhunt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private data class FlashStarParticle(
    val angleRad: Float,
    val distanceMul: Float,
    val size: Float,
    val colorIndex: Int,
    val twinklePhase: Float,
    val isTinyDot: Boolean,
    val upwardBias: Float,
)

private data class ZoomFlashPalette(
    val starColors: List<Color>,
    val outerBurst: Color,
    val innerBurst: Color,
    val coreBurst: Color,
)

private data class ZoomFlashVisualSpec(
    val maxRadiusFactor: Float,
    val durationMs: Int,
    val outerBurstRadiusMul: Float,
    val innerBurstRadiusMul: Float,
    val coreBurstRadiusMul: Float,
    val outerBurstAlpha: Float,
    val innerBurstAlpha: Float,
    val coreBurstAlpha: Float,
    val travelProgressPower: Float,
    val upSurgeFactor: Float,
    val starSizeScale: Float,
    val sparkleStarCount: Int,
    val tinyDotCount: Int,
    val midStarCount: Int,
    val distanceMulMin: Float,
    val distanceMulRange: Float,
    val tinyDistanceMulMin: Float,
    val tinyDistanceMulRange: Float,
    val midDistanceMulMin: Float,
    val midDistanceMulRange: Float,
)

private val dayFlashPalette = ZoomFlashPalette(
    starColors = listOf(
        Color(0xFF0D47A1),
        Color(0xFF1565C0),
        Color(0xFF1E3A8A),
        Color(0xFF42A5F5),
        Color(0xFFB3E5FC),
    ),
    outerBurst = Color(0xFF90CAF9),
    innerBurst = Color(0xFF1565C0),
    coreBurst = Color(0xFF0D47A1),
)

private val nightFlashPalette = ZoomFlashPalette(
    starColors = listOf(
        Color(0xFFFFD60A),
        Color(0xFFFFF8DC),
        Color(0xFFFFE082),
        Color(0xFFFFFFFF),
        Color(0xFFFFC107),
    ),
    outerBurst = Color(0xFFFFF8DC),
    innerBurst = Color(0xFFFFE082),
    coreBurst = Color(0xFFFFD60A),
)

private val dayFlashVisual = ZoomFlashVisualSpec(
    maxRadiusFactor = 0.86f,
    durationMs = 940,
    outerBurstRadiusMul = 1.48f,
    innerBurstRadiusMul = 1.08f,
    coreBurstRadiusMul = 0.62f,
    outerBurstAlpha = 0.4f,
    innerBurstAlpha = 0.26f,
    coreBurstAlpha = 0.34f,
    travelProgressPower = 0.68f,
    upSurgeFactor = 0.28f,
    starSizeScale = 1.55f,
    sparkleStarCount = 196,
    tinyDotCount = 176,
    midStarCount = 104,
    distanceMulMin = 0.22f,
    distanceMulRange = 1.62f,
    tinyDistanceMulMin = 0.1f,
    tinyDistanceMulRange = 1.78f,
    midDistanceMulMin = 0.42f,
    midDistanceMulRange = 1.28f,
)

private val nightFlashVisual = ZoomFlashVisualSpec(
    maxRadiusFactor = 0.88f,
    durationMs = 960,
    outerBurstRadiusMul = 1.52f,
    innerBurstRadiusMul = 1.1f,
    coreBurstRadiusMul = 0.66f,
    outerBurstAlpha = 0.38f,
    innerBurstAlpha = 0.24f,
    coreBurstAlpha = 0.32f,
    travelProgressPower = 0.66f,
    upSurgeFactor = 0.3f,
    starSizeScale = 1.6f,
    sparkleStarCount = 208,
    tinyDotCount = 188,
    midStarCount = 112,
    distanceMulMin = 0.24f,
    distanceMulRange = 1.68f,
    tinyDistanceMulMin = 0.12f,
    tinyDistanceMulRange = 1.82f,
    midDistanceMulMin = 0.44f,
    midDistanceMulRange = 1.32f,
)

@Composable
internal fun GoldHuntZoomThresholdFlashOverlay(
    triggerNonce: Int,
    isDarkAppearance: Boolean,
    modifier: Modifier = Modifier,
) {
    if (triggerNonce <= 0) return

    val palette = if (isDarkAppearance) nightFlashPalette else dayFlashPalette
    val visual = if (isDarkAppearance) nightFlashVisual else dayFlashVisual
    val progress = remember { Animatable(0f) }
    val animatedProgress by progress.asState()
    val stars = remember(triggerNonce, isDarkAppearance) {
        buildFlashStars(visual, palette.starColors.size)
    }

    LaunchedEffect(triggerNonce, isDarkAppearance) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = visual.durationMs,
                easing = FastOutSlowInEasing,
            ),
        )
        progress.snapTo(0f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (animatedProgress <= 0f) return@Canvas
        val center = Offset(size.width * 0.5f, size.height * 0.5f)
        val cornerReach = hypot(size.width.toDouble(), size.height.toDouble()).toFloat() * visual.maxRadiusFactor
        drawZoomThresholdFlash(
            center = center,
            maxRadius = cornerReach,
            progress = animatedProgress,
            stars = stars,
            palette = palette,
            visual = visual,
        )
    }
}

private fun buildFlashStars(visual: ZoomFlashVisualSpec, colorCount: Int): List<FlashStarParticle> {
    return buildList {
        repeat(visual.sparkleStarCount) {
            add(
                FlashStarParticle(
                    angleRad = Random.nextFloat() * (Math.PI * 2).toFloat(),
                    distanceMul = visual.distanceMulMin + Random.nextFloat() * visual.distanceMulRange,
                    size = (2.4f + Random.nextFloat() * 4.8f) * visual.starSizeScale,
                    colorIndex = Random.nextInt(colorCount),
                    twinklePhase = Random.nextFloat(),
                    isTinyDot = false,
                    upwardBias = 0.35f + Random.nextFloat() * 0.65f,
                ),
            )
        }
        repeat(visual.tinyDotCount) {
            add(
                FlashStarParticle(
                    angleRad = Random.nextFloat() * (Math.PI * 2).toFloat(),
                    distanceMul = visual.tinyDistanceMulMin + Random.nextFloat() * visual.tinyDistanceMulRange,
                    size = (1.1f + Random.nextFloat() * 2.6f) * visual.starSizeScale,
                    colorIndex = Random.nextInt(colorCount),
                    twinklePhase = Random.nextFloat(),
                    isTinyDot = true,
                    upwardBias = 0.4f + Random.nextFloat() * 0.6f,
                ),
            )
        }
        repeat(visual.midStarCount) {
            add(
                FlashStarParticle(
                    angleRad = Random.nextFloat() * (Math.PI * 2).toFloat(),
                    distanceMul = visual.midDistanceMulMin + Random.nextFloat() * visual.midDistanceMulRange,
                    size = (1.8f + Random.nextFloat() * 3.2f) * visual.starSizeScale,
                    colorIndex = Random.nextInt(colorCount),
                    twinklePhase = Random.nextFloat(),
                    isTinyDot = false,
                    upwardBias = 0.5f + Random.nextFloat() * 0.5f,
                ),
            )
        }
    }
}

private fun DrawScope.drawZoomThresholdFlash(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    stars: List<FlashStarParticle>,
    palette: ZoomFlashPalette,
    visual: ZoomFlashVisualSpec,
) {
    val fade = (1f - progress).coerceIn(0f, 1f)
    val burst = (1f - (progress - 0.06f).coerceIn(0f, 1f)).coerceIn(0f, 1f)
    val upSurge = progress.pow(visual.travelProgressPower) * maxRadius * visual.upSurgeFactor
    val burstCenter = Offset(center.x, center.y - upSurge * 0.42f)
    val travelProgress = progress.pow(visual.travelProgressPower)

    drawCircle(
        color = palette.outerBurst.copy(alpha = burst * visual.outerBurstAlpha),
        radius = maxRadius * visual.outerBurstRadiusMul,
        center = burstCenter,
    )
    drawCircle(
        color = palette.innerBurst.copy(alpha = burst * visual.innerBurstAlpha),
        radius = maxRadius * visual.innerBurstRadiusMul,
        center = burstCenter,
    )
    if (visual.coreBurstRadiusMul > 0f) {
        drawCircle(
            color = palette.coreBurst.copy(alpha = burst * visual.coreBurstAlpha),
            radius = maxRadius * visual.coreBurstRadiusMul + progress * maxRadius * 0.18f,
            center = burstCenter,
        )
    }

    stars.forEach { star ->
        val travel = travelProgress * maxRadius * star.distanceMul
        val x = center.x + cos(star.angleRad) * travel
        val y = center.y + sin(star.angleRad) * travel - upSurge * star.upwardBias
        val twinkleWave = sin(((progress + star.twinklePhase) * 22f).toDouble()).toFloat()
        val twinkle = (
            fade *
                (0.52f + 0.48f * ((twinkleWave.coerceIn(-1f, 1f) + 1f) * 0.5f))
            ).coerceIn(0f, 1f)
        val color = palette.starColors[star.colorIndex % palette.starColors.size]
        if (star.isTinyDot) {
            drawCircle(
                color = color.copy(alpha = twinkle * 0.9f),
                radius = star.size * (1f - progress * 0.1f),
                center = Offset(x, y),
            )
            drawCircle(
                color = color.copy(alpha = twinkle * 0.34f),
                radius = star.size * 2.05f,
                center = Offset(x, y),
            )
        } else {
            drawTinySparkleStar(
                center = Offset(x, y),
                radius = star.size * (1f - progress * 0.14f),
                color = color.copy(alpha = twinkle * 0.96f),
            )
        }
    }
}

private fun DrawScope.drawTinySparkleStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    if (radius <= 0.5f || color.alpha <= 0f) return

    drawCircle(
        color = color.copy(alpha = color.alpha * 0.35f),
        radius = radius * 1.35f,
        center = center,
    )
    drawCircle(
        color = color,
        radius = radius * 0.42f,
        center = center,
    )

    val arm = radius * 1.15f
    val path = Path().apply {
        moveTo(center.x, center.y - arm)
        lineTo(center.x + radius * 0.22f, center.y - radius * 0.22f)
        lineTo(center.x + arm, center.y)
        lineTo(center.x + radius * 0.22f, center.y + radius * 0.22f)
        lineTo(center.x, center.y + arm)
        lineTo(center.x - radius * 0.22f, center.y + radius * 0.22f)
        lineTo(center.x - arm, center.y)
        lineTo(center.x - radius * 0.22f, center.y - radius * 0.22f)
        close()
    }
    drawPath(path, color.copy(alpha = color.alpha * 0.82f))
}
