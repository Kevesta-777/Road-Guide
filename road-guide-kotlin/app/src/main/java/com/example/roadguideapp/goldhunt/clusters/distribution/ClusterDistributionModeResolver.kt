package com.example.roadguideapp.goldhunt.clusters.distribution

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/** Maps cluster families to default distribution strategies. */
internal object ClusterDistributionModeResolver {
    fun defaultMode(clusterType: ClusterType): ClusterTreasureDistributionMode = when (clusterType) {
        ClusterType.ROADSIDE_CACHE -> ClusterTreasureDistributionMode.ROAD_BIASED
        ClusterType.EXPLORER_NEST -> ClusterTreasureDistributionMode.NATURAL_SCATTER
        ClusterType.TREASURE_GARDEN -> ClusterTreasureDistributionMode.CIRCLE
        ClusterType.ANCIENT_VAULT -> ClusterTreasureDistributionMode.POI_BIASED
        ClusterType.LEGENDARY_HOARD -> ClusterTreasureDistributionMode.CIRCLE
    }

    fun resolveMode(
        clusterType: ClusterType,
        clusterSeed: String,
    ): ClusterTreasureDistributionMode {
        val roll = TreasureHash.unitFraction("$clusterSeed:distributionMode")
        val preferred = defaultMode(clusterType)
        if (roll < 0.82) return preferred
        return when (preferred) {
            ClusterTreasureDistributionMode.CIRCLE ->
                ClusterTreasureDistributionMode.NATURAL_SCATTER
            ClusterTreasureDistributionMode.NATURAL_SCATTER ->
                ClusterTreasureDistributionMode.CIRCLE
            ClusterTreasureDistributionMode.ROAD_BIASED ->
                ClusterTreasureDistributionMode.NATURAL_SCATTER
            ClusterTreasureDistributionMode.POI_BIASED ->
                ClusterTreasureDistributionMode.NATURAL_SCATTER
        }
    }
}
