package com.example.roadguideapp.map

import android.os.SystemClock
import android.view.Choreographer
import org.maplibre.android.geometry.LatLng

internal data class DirectionsNavFrame(
    val lat: Double,
    val lng: Double,
    val bearingDegrees: Double,
    /** Engine distance along the route (simulation / progress). */
    val cumulativeDistanceM: Double,
    /** Distance used to slice the remaining route line (matches [lat]/[lng]). */
    val routeSliceDistanceM: Double = cumulativeDistanceM,
)

internal class DirectionsNavigationEngine(
    private val onUpdate: (DirectionsNavFrame, Float) -> Unit,
    private val onRouteComplete: () -> Unit = {},
) {
    private data class NavSample(
        val lat: Double,
        val lng: Double,
        val bearing: Double,
        val cumulativeM: Double,
    )

    var speedMps: Double = DirectionsNavConfig.DRIVE_SPEED_MPS

    private var samples: List<NavSample> = emptyList()
    private var totalLengthM: Double = 0.0
    private var distanceAlongM: Double = 0.0
    private var running = false
    private var lastFrameTimeNs: Long = 0L
    private var routeCompleteDispatched = false
    private var startedAtMs: Long = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running || samples.size < 2) return

            val dt = if (lastFrameTimeNs == 0L) {
                0.016
            } else {
                ((frameTimeNanos - lastFrameTimeNs) / 1_000_000_000.0).coerceIn(0.001, 0.05)
            }
            lastFrameTimeNs = frameTimeNanos

            distanceAlongM = (distanceAlongM + speedMps * dt).coerceAtMost(totalLengthM)
            emitAtDistance(distanceAlongM)

            val atEnd = distanceAlongM >= (totalLengthM - 1.0).coerceAtLeast(0.0)
            val droveLongEnough = SystemClock.uptimeMillis() - startedAtMs >= 2_000L
            if (running && (!atEnd || !droveLongEnough)) {
                Choreographer.getInstance().postFrameCallback(this)
            } else if (running) {
                running = false
                emitAtDistance(totalLengthM)
                if (!routeCompleteDispatched && totalLengthM >= 10.0) {
                    routeCompleteDispatched = true
                    onRouteComplete()
                }
            }
        }
    }

    fun loadGeometry(geometry: List<LatLng>, resetPosition: Boolean = true) {
        pause()
        samples = buildSamples(geometry)
        totalLengthM = samples.lastOrNull()?.cumulativeM ?: 0.0
        if (resetPosition || distanceAlongM > totalLengthM) {
            distanceAlongM = 0.0
        }
    }

    fun start(): Boolean {
        if (samples.size < 2 || totalLengthM < 10.0) return false
        routeCompleteDispatched = false
        running = true
        startedAtMs = SystemClock.uptimeMillis()
        lastFrameTimeNs = 0L
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        Choreographer.getInstance().postFrameCallback(frameCallback)
        emitAtDistance(distanceAlongM)
        return true
    }

    fun pause() {
        running = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        lastFrameTimeNs = 0L
    }

    fun stop() {
        pause()
        distanceAlongM = 0.0
    }

    fun isRunning(): Boolean = running

    fun currentDistanceM(): Double = distanceAlongM

    fun routeLengthM(): Double = totalLengthM

    fun progress(): Float =
        if (totalLengthM < 0.1) 0f else (distanceAlongM / totalLengthM).toFloat().coerceIn(0f, 1f)

    private fun buildSamples(geometry: List<LatLng>): List<NavSample> {
        if (geometry.size < 2) return emptyList()
        val out = ArrayList<NavSample>(geometry.size)
        var cumulative = 0.0
        var prev = geometry.first()
        geometry.forEachIndexed { index, point ->
            if (index > 0) {
                cumulative += haversineMetersNav(
                    prev.longitude,
                    prev.latitude,
                    point.longitude,
                    point.latitude,
                )
            }
            val bearing = if (index < geometry.lastIndex) {
                bearingDegrees(
                    point.latitude,
                    point.longitude,
                    geometry[index + 1].latitude,
                    geometry[index + 1].longitude,
                )
            } else {
                out.lastOrNull()?.bearing ?: 0.0
            }
            out.add(NavSample(point.latitude, point.longitude, bearing, cumulative))
            prev = point
        }
        return out
    }

    private fun emitAtDistance(distanceM: Double) {
        val sample = sampleAt(distanceM)
        val progress = progress()
        onUpdate(
            DirectionsNavFrame(
                lat = sample.lat,
                lng = sample.lng,
                bearingDegrees = sample.bearing,
                cumulativeDistanceM = distanceM,
            ),
            progress,
        )
    }

    private fun sampleAt(distanceM: Double): NavSample {
        if (samples.isEmpty()) return NavSample(0.0, 0.0, 0.0, 0.0)
        if (distanceM <= 0.0) return samples.first()
        if (distanceM >= totalLengthM) return samples.last()

        var i = 0
        while (i < samples.size - 1 && samples[i + 1].cumulativeM < distanceM) {
            i++
        }
        val a = samples[i]
        val b = samples[i + 1]
        val segLen = (b.cumulativeM - a.cumulativeM).coerceAtLeast(0.001)
        val alongM = (distanceM - a.cumulativeM).coerceIn(0.0, segLen)
        val segBearing = bearingDegrees(a.lat, a.lng, b.lat, b.lng)
        val (lat, lng) = destinationPointNav(a.lat, a.lng, segBearing, alongM)
        return NavSample(
            lat = lat,
            lng = lng,
            bearing = segBearing,
            cumulativeM = distanceM,
        )
    }
}
