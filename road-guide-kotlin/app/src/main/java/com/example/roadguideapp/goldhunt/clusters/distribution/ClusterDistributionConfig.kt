package com.example.roadguideapp.goldhunt.clusters.distribution

/** Tuning for cluster treasure placement. */
internal object ClusterDistributionConfig {
    /** Minimum center-to-center separation between cluster treasures (meters). */
    const val MIN_SEPARATION_M = 14.0

    /** Fraction of cluster radius reserved as edge margin (treasures stay inside). */
    const val EDGE_MARGIN_FRACTION = 0.12

    /** Deterministic retry budget per treasure slot when avoiding overlap. */
    const val MAX_PLACEMENT_ATTEMPTS = 24

    /** Max offset from a road/POI anchor when biasing (meters). */
    const val ANCHOR_JITTER_M = 18.0

    /** Circle layout: inner ring fraction of effective radius. */
    const val CIRCLE_MIN_RADIUS_FRACTION = 0.42

    /** Circle layout: outer ring fraction of effective radius. */
    const val CIRCLE_MAX_RADIUS_FRACTION = 0.88
}
