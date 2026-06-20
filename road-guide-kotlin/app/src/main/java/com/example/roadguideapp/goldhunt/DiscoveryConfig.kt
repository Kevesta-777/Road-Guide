package com.example.roadguideapp.goldhunt

/**
 * Tuning for fog-of-discovery stamping and location sampling.
 */
internal object DiscoveryConfig {

    /** Grid cell size in degrees (~130 m latitude at the equator). */
    const val CELL_SIZE_DEG: Double = 0.0012

    /** H3-style k-ring: 1 → 3×3 neighborhood stamped per accepted fix. */
    const val STAMP_RING: Int = 1

    const val MAX_ACCURACY_METERS: Float = 80f

    /** Minimum displacement between accepted GPS samples. */
    const val MIN_STAMP_DISTANCE_METERS: Double = 28.0

    /** Max interval while moving before forcing a stamp (ms). */
    const val MAX_STAMP_INTERVAL_MS: Long = 12_000L

    const val LOCATION_MIN_TIME_MS: Long = 2_500L

    const val LOCATION_MIN_DISTANCE_METERS: Float = 20f

    const val FLUSH_PENDING_AFTER_CELLS: Int = 32

    const val FLUSH_INTERVAL_MS: Long = 8_000L

    const val MAX_FOG_FEATURES_PER_SYNC: Int = 2_500

    /** Fog fill opacity when Gold Hunt is active (explore mode). */
    const val FOG_OPACITY_EXPLORE: Float = 0.58f

    /** Reduced opacity during turn-by-turn so the route stays readable. */
    const val FOG_OPACITY_NAVIGATION: Float = 0.22f
}


