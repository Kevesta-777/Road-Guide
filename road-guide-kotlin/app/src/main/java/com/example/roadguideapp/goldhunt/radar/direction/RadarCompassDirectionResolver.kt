package com.example.roadguideapp.goldhunt.radar.direction

/**
 * Maps a true bearing into one of eight compass sectors (45° each, north-centered).
 */
internal object RadarCompassDirectionResolver {
    private const val SECTOR_WIDTH_DEG = 45.0
    private const val SECTOR_OFFSET_DEG = 22.5

    fun fromBearingDegrees(bearingDegrees: Double): RadarCompassDirection {
        val normalized = RadarBearingCalculator.normalizeDegrees(bearingDegrees)
        val sectorIndex = (
            ((normalized + SECTOR_OFFSET_DEG) % 360.0) / SECTOR_WIDTH_DEG
            ).toInt() % RadarCompassDirection.ALL_ORDERED.size
        return RadarCompassDirection.fromSectorIndex(sectorIndex)
    }

    fun sectorCenterBearing(direction: RadarCompassDirection): Double =
        direction.sectorIndex * SECTOR_WIDTH_DEG
}
