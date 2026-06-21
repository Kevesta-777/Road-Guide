package com.example.roadguideapp.goldhunt.clusters.discovery

/** Tuning for treasure cluster discovery triggers. */
internal object ClusterDiscoveryConfig {
    /** Player must be within this distance of cluster center (meters), or within cluster radius if smaller. */
    const val DISTANCE_THRESHOLD_M = 80.0

    /** Explored L0 cells required inside cluster radius for exploration-based discovery. */
    const val MIN_EXPLORED_L0_CELLS = 2

    /** Scan radius around the player when evaluating nearby clusters. */
    const val NEARBY_SCAN_RADIUS_M = 280.0
}
