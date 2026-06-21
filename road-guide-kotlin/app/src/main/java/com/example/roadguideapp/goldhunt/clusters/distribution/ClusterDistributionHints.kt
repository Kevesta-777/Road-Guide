package com.example.roadguideapp.goldhunt.clusters.distribution

/**
 * Optional map hints for road- and POI-biased layouts.
 * Empty lists fall back to deterministic natural scatter.
 */
internal data class ClusterDistributionHints(
    val roadAnchors: List<ClusterAnchorPoint> = emptyList(),
    val poiAnchors: List<ClusterAnchorPoint> = emptyList(),
) {
    companion object {
        val EMPTY = ClusterDistributionHints()
    }
}

internal data class ClusterAnchorPoint(
    val latitude: Double,
    val longitude: Double,
    val weight: Double = 1.0,
)
