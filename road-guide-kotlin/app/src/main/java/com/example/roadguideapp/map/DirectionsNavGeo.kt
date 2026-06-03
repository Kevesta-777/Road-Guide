package com.example.roadguideapp.map

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun bearingDegrees(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val dL = Math.toRadians(lng2 - lng1)
    val y = sin(dL) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dL)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

internal fun deltaAngleDegrees(from: Double, to: Double): Double {
    var d = (to - from) % 360.0
    if (d > 180.0) d -= 360.0
    if (d < -180.0) d += 360.0
    return d
}

internal fun lerpBearingDegrees(from: Double, to: Double, t: Double): Double {
    val d = deltaAngleDegrees(from, to)
    return (from + d * t + 360.0) % 360.0
}

internal fun haversineMetersNav(lng1: Double, lat1: Double, lng2: Double, lat2: Double): Double {
    val r = 6_371_000.0
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val dPhi = Math.toRadians(lat2 - lat1)
    val dL = Math.toRadians(lng2 - lng1)
    val a = sin(dPhi / 2) * sin(dPhi / 2) + cos(phi1) * cos(phi2) * sin(dL / 2) * sin(dL / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

internal fun destinationPointNav(
    lat: Double,
    lng: Double,
    bearingDeg: Double,
    distanceM: Double,
): Pair<Double, Double> {
    if (distanceM <= 0.0) return lat to lng
    val earthR = 6_371_000.0
    val brng = Math.toRadians(bearingDeg)
    val lat1 = Math.toRadians(lat)
    val lng1 = Math.toRadians(lng)
    val angDist = distanceM / earthR
    val lat2 = kotlin.math.asin(
        sin(lat1) * cos(angDist) + cos(lat1) * sin(angDist) * cos(brng),
    )
    val lng2 = lng1 + atan2(
        sin(brng) * sin(angDist) * cos(lat1),
        cos(angDist) - sin(lat1) * sin(lat2),
    )
    return Math.toDegrees(lat2) to ((Math.toDegrees(lng2) + 540.0) % 360.0 - 180.0)
}

internal fun lerpAngleDegreesNav(from: Double, to: Double, t: Double): Double {
    val d = deltaAngleDegrees(from, to)
    return (from + d * t.coerceIn(0.0, 1.0) + 360.0) % 360.0
}
