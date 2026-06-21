package com.example.roadguideapp.goldhunt.clusters

/** Offline tuning for procedural treasure cluster generation. */
internal object ClusterGenerationConfig {
    const val GENERATOR_VERSION = 1

    /** Per-tile spawn chance (deterministic roll must be <= this to place a cluster). */
    const val PROCEDURAL_SPAWN_CHANCE = 0.06

    const val MIN_RADIUS_M = 80.0
    const val MAX_RADIUS_M = 180.0

    /** In-cell margin fraction when placing cluster center (matches treasure placement style). */
    const val CENTER_MARGIN = 0.18

    /** One cluster anchor slot per L1 tile during viewport scans. */
    const val CLUSTER_SLOT_PER_L1 = 0
}
