package com.example.roadguideapp.goldhunt.clusters.distribution

/** Layout strategy for treasures inside a cluster radius. */
internal enum class ClusterTreasureDistributionMode {
    CIRCLE,
    NATURAL_SCATTER,
    ROAD_BIASED,
    POI_BIASED,
    ;

    val id: String get() = name
}
