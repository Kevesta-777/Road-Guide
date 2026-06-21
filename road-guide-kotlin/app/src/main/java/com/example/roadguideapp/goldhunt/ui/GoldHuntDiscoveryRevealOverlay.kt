package com.example.roadguideapp.goldhunt.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.overlays.GoldHuntMarkerIcons
import com.example.roadguideapp.goldhunt.overlays.GoldHuntTreasureMapStyle
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private data class SparkleParticle(
    val angleRad: Float,
    val speed: Float,
    val size: Float,
    val colorIndex: Int,
    val twinklePhase: Float,
)

private data class FlyingStarParticle(
    val angleRad: Float,
    val orbitSpeed: Float,
    val distanceMul: Float,
    val size: Float,
    val colorIndex: Int,
    val twinklePhase: Float,
)

private val SoftSparkleEasing = CubicBezierEasing(0.33f, 0f, 0.2f, 1f)
private val SoftPopEasing = LinearOutSlowInEasing

@Composable
internal fun GoldHuntDiscoveryRevealOverlay(
    map: MapLibreMap?,
    mapView: MapView?,
    cameraTick: Int,
    targetLatLng: LatLng?,
    visible: Boolean,
    sparkleStyle: GoldHuntCollectSparkleStyle = GoldHuntCollectSparkleStyle.STAR,
    treasureType: TreasureType? = null,
    showMarkerPop: Boolean = false,
    display3d: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (!visible || map == null || mapView == null || targetLatLng == null) return

    val density = LocalDensity.current
    val densityScale = density.density
    val palette = remember(sparkleStyle) {
        GoldHuntCollectSparklePalettes.forStyle(sparkleStyle)
    }
    val sparkleScale = GoldHuntCollectSparkleVisuals.SIZE_SCALE
    val particles = remember(sparkleStyle) {
        List(GoldHuntCollectSparkleVisuals.PARTICLE_COUNT) { index ->
            val baseAngle = (index.toFloat() / GoldHuntCollectSparkleVisuals.PARTICLE_COUNT.toFloat()) *
                (Math.PI * 2).toFloat()
            SparkleParticle(
                angleRad = baseAngle + Random.nextFloat() * 0.35f,
                speed = 0.32f + Random.nextFloat() * 0.42f,
                size = (4.5f + Random.nextFloat() * 4.5f) * sparkleScale,
                colorIndex = index % palette.particleColors.size,
                twinklePhase = Random.nextFloat(),
            )
        }
    }
    val flyingStars = remember(sparkleStyle) {
        List(GoldHuntCollectSparkleVisuals.TINY_STAR_COUNT) { index ->
            FlyingStarParticle(
                angleRad = Random.nextFloat() * (Math.PI * 2).toFloat(),
                orbitSpeed = 0.9f + Random.nextFloat() * 2.2f,
                distanceMul = 0.28f + Random.nextFloat() * 1.05f,
                size = (1.6f + Random.nextFloat() * 2.8f) * sparkleScale,
                colorIndex = index % palette.particleColors.size,
                twinklePhase = Random.nextFloat(),
            )
        }
    }
    val popProgress = remember { Animatable(0f) }
    val sparkleProgress = remember { Animatable(0f) }

    LaunchedEffect(visible, sparkleStyle, treasureType, showMarkerPop) {
        if (visible) {
            popProgress.snapTo(0f)
            sparkleProgress.snapTo(0f)
            coroutineScope {
                val sparkleJob = launch {
                    sparkleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = GoldHuntCollectEffectTiming.SPARKLE_DURATION_MS,
                            easing = SoftSparkleEasing,
                        ),
                    )
                }
                val popJob = if (showMarkerPop) {
                    launch {
                        popProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = GoldHuntCollectEffectTiming.POP_DURATION_MS,
                                easing = SoftPopEasing,
                            ),
                        )
                    }
                } else {
                    null
                }
                sparkleJob.join()
                popJob?.join()
            }
        } else {
            popProgress.snapTo(0f)
            sparkleProgress.snapTo(0f)
        }
    }

    val screenPoint = remember(map, mapView, cameraTick, targetLatLng, display3d, densityScale) {
        val point = map.projection.toScreenLocation(targetLatLng)
        val centerYOffset = GoldHuntTreasureMapStyle.iconCenterOffsetYPx(display3d, densityScale)
        Offset(point.x, point.y + centerYOffset)
    }

    val markerDrawable = when {
        treasureType != null -> GoldHuntMarkerIcons.treasureDrawableRes(treasureType)
        sparkleStyle == GoldHuntCollectSparkleStyle.SECRET -> R.drawable.secret_place
        else -> null
    }
    val markerBaseSizeDp = 52.dp
    val popScale = if (showMarkerPop && markerDrawable != null) {
        val easedPop = softEaseOut(popProgress.value)
        1f + easedPop
    } else {
        1f
    }
    val markerAlpha = if (showMarkerPop && markerDrawable != null) {
        val fadeStart = GoldHuntCollectEffectTiming.MARKER_FADE_START_PROGRESS
        val t = sparkleProgress.value
        if (t <= fadeStart) {
            1f
        } else {
            val fadeT = ((t - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
            (1f - fadeT * fadeT).coerceIn(0f, 1f)
        }
    } else {
        0f
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (markerDrawable != null && showMarkerPop && markerAlpha > 0f) {
            val markerSizePx = with(density) { markerBaseSizeDp.toPx() * popScale }
            val half = markerSizePx * 0.5f
            Image(
                painter = painterResource(markerDrawable),
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (screenPoint.x - half).roundToInt(),
                            (screenPoint.y - half).roundToInt(),
                        )
                    }
                    .size(with(density) { markerBaseSizeDp * popScale })
                    .graphicsLayer { alpha = markerAlpha },
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDiscoveryReveal(
                center = screenPoint,
                progress = sparkleProgress.value,
                particles = particles,
                flyingStars = flyingStars,
                palette = palette,
                multiColor = sparkleStyle == GoldHuntCollectSparkleStyle.SECRET,
                sizeScale = sparkleScale,
            )
        }
    }
}

private fun softEaseOut(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return 1f - (1f - x) * (1f - x)
}

private fun softSparkleProgress(progress: Float): Float = softEaseOut(progress)

private fun DrawScope.drawDiscoveryReveal(
    center: Offset,
    progress: Float,
    particles: List<SparkleParticle>,
    flyingStars: List<FlyingStarParticle>,
    palette: GoldHuntCollectSparklePalette,
    multiColor: Boolean,
    sizeScale: Float,
) {
    if (progress <= 0f) return

    val eased = softSparkleProgress(progress)
    val fade = (1f - progress).coerceIn(0f, 1f)
    val ringRadius = (24f + eased * 128f) * sizeScale
    val ringAlpha = (fade * fade * 0.48f).coerceIn(0f, 1f)

    if (multiColor) {
        val secretRingColors = listOf(
            palette.particleColors[0],
            palette.particleColors[1],
            palette.particleColors[2],
            palette.particleColors[3],
        )
        secretRingColors.forEachIndexed { index, color ->
            val offset = index * 4f * sizeScale
            drawCircle(
                color = color.copy(alpha = ringAlpha * 0.14f),
                radius = ringRadius + offset,
                center = center,
            )
            drawCircle(
                color = color.copy(alpha = ringAlpha * 0.28f),
                radius = ringRadius + offset,
                center = center,
                style = Stroke(width = 1.25f * sizeScale),
            )
        }
    } else {
        drawCircle(
            color = palette.ringPrimary.copy(alpha = ringAlpha * 0.16f),
            radius = ringRadius * 1.08f,
            center = center,
        )
        drawCircle(
            color = palette.ringSecondary.copy(alpha = ringAlpha * 0.1f),
            radius = ringRadius * 0.72f,
            center = center,
        )
        drawCircle(
            color = palette.ringPrimary.copy(alpha = ringAlpha * 0.32f),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1.5f * sizeScale),
        )
    }

    particles.forEach { particle ->
        val distance = eased * ringRadius * particle.speed
        val x = center.x + cos(particle.angleRad) * distance
        val y = center.y + sin(particle.angleRad) * distance
        val twinkleWave = sin(((progress + particle.twinklePhase) * 10f).toDouble()).toFloat()
        val twinkle = 0.58f + 0.42f * ((twinkleWave.coerceIn(-1f, 1f) + 1f) * 0.5f)
        val alpha = (fade * fade * twinkle * 0.62f).coerceIn(0f, 1f)
        val color = palette.particleColors[particle.colorIndex % palette.particleColors.size]
        drawCircle(
            color = color.copy(alpha = alpha * 0.28f),
            radius = particle.size * 1.35f * (1f - progress * 0.12f),
            center = Offset(x, y),
        )
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = particle.size * (1f - progress * 0.15f),
            center = Offset(x, y),
        )
    }

    flyingStars.forEach { star ->
        val orbitAngle = star.angleRad + progress * star.orbitSpeed * 5.5f
        val flyRadius = (20f + eased * 96f) * sizeScale * star.distanceMul
        val x = center.x + cos(orbitAngle) * flyRadius
        val y = center.y + sin(orbitAngle) * flyRadius
        val twinkleWave = sin(((progress + star.twinklePhase) * 14f).toDouble()).toFloat()
        val twinkle = 0.5f + 0.5f * ((twinkleWave.coerceIn(-1f, 1f) + 1f) * 0.5f)
        val alpha = (fade * fade * twinkle * 0.74f).coerceIn(0f, 1f)
        val color = palette.particleColors[star.colorIndex % palette.particleColors.size]
        drawCollectTinyStar(
            center = Offset(x, y),
            radius = star.size * (1f - progress * 0.1f),
            color = color.copy(alpha = alpha),
        )
    }

    val coreAlpha = (fade * fade * 0.55f).coerceIn(0f, 1f)
    if (coreAlpha > 0f) {
        drawCircle(
            color = palette.core.copy(alpha = coreAlpha * 0.42f),
            radius = (18f + eased * 16f) * sizeScale,
            center = center,
        )
        drawCircle(
            color = palette.core.copy(alpha = coreAlpha * 0.68f),
            radius = (10f + eased * 8f) * sizeScale,
            center = center,
        )
    }
}

private fun DrawScope.drawCollectTinyStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    if (radius <= 0.4f || color.alpha <= 0f) return

    drawCircle(
        color = color.copy(alpha = color.alpha * 0.32f),
        radius = radius * 1.4f,
        center = center,
    )
    drawCircle(
        color = color,
        radius = radius * 0.38f,
        center = center,
    )

    val arm = radius * 1.1f
    val path = Path().apply {
        moveTo(center.x, center.y - arm)
        lineTo(center.x + radius * 0.2f, center.y - radius * 0.2f)
        lineTo(center.x + arm, center.y)
        lineTo(center.x + radius * 0.2f, center.y + radius * 0.2f)
        lineTo(center.x, center.y + arm)
        lineTo(center.x - radius * 0.2f, center.y + radius * 0.2f)
        lineTo(center.x - arm, center.y)
        lineTo(center.x - radius * 0.2f, center.y - radius * 0.2f)
        close()
    }
    drawPath(path, color.copy(alpha = color.alpha * 0.8f))
}
