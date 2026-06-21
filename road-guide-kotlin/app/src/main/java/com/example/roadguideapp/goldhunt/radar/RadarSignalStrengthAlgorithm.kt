package com.example.roadguideapp.goldhunt.radar

/**
 * Distance-based radar signal strength curve (offline, all [RadarType] families).
 *
 * Canonical anchor examples (before range cutoff):
 * - 0 m   → 100%
 * - 100 m → 80%
 * - 300 m → 50%
 * - 600 m → 20%
 *
 * Strength linearly interpolates between anchors, then falls to 0% at the equipped
 * radar's [maxRangeMeters]. Targets beyond max range return 0%.
 */
internal object RadarSignalStrengthAlgorithm {
    private data class Anchor(
        val distanceMeters: Double,
        val strength: Double,
    )

    private val distanceAnchors: List<Anchor> = listOf(
        Anchor(distanceMeters = 0.0, strength = 1.0),
        Anchor(distanceMeters = 100.0, strength = 0.8),
        Anchor(distanceMeters = 300.0, strength = 0.5),
        Anchor(distanceMeters = 600.0, strength = 0.2),
    )

    fun strength(
        distanceMeters: Double,
        maxRangeMeters: Int,
    ): Double {
        if (maxRangeMeters <= 0 || distanceMeters < 0.0) {
            return RadarSchema.MIN_SIGNAL_STRENGTH
        }
        val maxRange = maxRangeMeters.toDouble()
        if (distanceMeters > maxRange) {
            return RadarSchema.MIN_SIGNAL_STRENGTH
        }
        if (distanceMeters == 0.0) {
            return RadarSchema.MAX_SIGNAL_STRENGTH
        }

        val anchors = effectiveAnchors(maxRange)
        for (index in 0 until anchors.lastIndex) {
            val start = anchors[index]
            val end = anchors[index + 1]
            if (distanceMeters <= end.distanceMeters) {
                return interpolate(
                    distanceMeters = distanceMeters,
                    startDistance = start.distanceMeters,
                    startStrength = start.strength,
                    endDistance = end.distanceMeters,
                    endStrength = end.strength,
                )
            }
        }
        return anchors.last().strength.coerceIn(
            RadarSchema.MIN_SIGNAL_STRENGTH,
            RadarSchema.MAX_SIGNAL_STRENGTH,
        )
    }

    fun strengthForRadarType(
        distanceMeters: Double,
        radarType: RadarType,
    ): Double = strength(distanceMeters, radarType.detectionRangeMeters)

    fun strengthPercent(
        distanceMeters: Double,
        maxRangeMeters: Int,
    ): Int = (strength(distanceMeters, maxRangeMeters) * 100.0)
        .toInt()
        .coerceIn(0, 100)

    fun strengthPercentForRadarType(
        distanceMeters: Double,
        radarType: RadarType,
    ): Int = strengthPercent(distanceMeters, radarType.detectionRangeMeters)

    fun isWithinRange(distanceMeters: Double, maxRangeMeters: Int): Boolean =
        distanceMeters >= 0.0 && maxRangeMeters > 0 && distanceMeters <= maxRangeMeters.toDouble()

    private fun effectiveAnchors(maxRangeMeters: Double): List<Anchor> {
        val withinRange = distanceAnchors.filter { it.distanceMeters <= maxRangeMeters }
        if (withinRange.isEmpty()) {
            return listOf(
                Anchor(distanceMeters = 0.0, strength = 1.0),
                Anchor(distanceMeters = maxRangeMeters, strength = 0.0),
            )
        }
        val lastAnchor = withinRange.last()
        return if (lastAnchor.distanceMeters == maxRangeMeters) {
            withinRange
        } else {
            withinRange + Anchor(distanceMeters = maxRangeMeters, strength = 0.0)
        }
    }

    private fun interpolate(
        distanceMeters: Double,
        startDistance: Double,
        startStrength: Double,
        endDistance: Double,
        endStrength: Double,
    ): Double {
        if (endDistance <= startDistance) {
            return startStrength.coerceIn(
                RadarSchema.MIN_SIGNAL_STRENGTH,
                RadarSchema.MAX_SIGNAL_STRENGTH,
            )
        }
        val progress = ((distanceMeters - startDistance) / (endDistance - startDistance))
            .coerceIn(0.0, 1.0)
        val value = startStrength + progress * (endStrength - startStrength)
        return value.coerceIn(RadarSchema.MIN_SIGNAL_STRENGTH, RadarSchema.MAX_SIGNAL_STRENGTH)
    }
}
