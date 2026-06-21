package com.example.roadguideapp.goldhunt.clusters.statistics

import com.example.roadguideapp.goldhunt.clusters.ClusterType

/**
 * Lifetime treasure cluster statistics persisted on the explorer profile.
 */
internal data class TreasureClusterStatistics(
    val clustersFound: Int = 0,
    val clustersCompleted: Int = 0,
    val roadsideCachesFound: Int = 0,
    val explorerNestsFound: Int = 0,
    val treasureGardensFound: Int = 0,
    val ancientVaultsFound: Int = 0,
    val legendaryHoardsFound: Int = 0,
    val roadsideCachesCompleted: Int = 0,
    val explorerNestsCompleted: Int = 0,
    val treasureGardensCompleted: Int = 0,
    val ancientVaultsCompleted: Int = 0,
    val legendaryHoardsCompleted: Int = 0,
    val totalRewardsEarned: Int = 0,
) {
    fun foundCountFor(type: ClusterType): Int = when (type) {
        ClusterType.ROADSIDE_CACHE -> roadsideCachesFound
        ClusterType.EXPLORER_NEST -> explorerNestsFound
        ClusterType.TREASURE_GARDEN -> treasureGardensFound
        ClusterType.ANCIENT_VAULT -> ancientVaultsFound
        ClusterType.LEGENDARY_HOARD -> legendaryHoardsFound
    }

    fun completedCountFor(type: ClusterType): Int = when (type) {
        ClusterType.ROADSIDE_CACHE -> roadsideCachesCompleted
        ClusterType.EXPLORER_NEST -> explorerNestsCompleted
        ClusterType.TREASURE_GARDEN -> treasureGardensCompleted
        ClusterType.ANCIENT_VAULT -> ancientVaultsCompleted
        ClusterType.LEGENDARY_HOARD -> legendaryHoardsCompleted
    }

    fun withDiscovery(type: ClusterType): TreasureClusterStatistics = copy(
        clustersFound = clustersFound + 1,
        roadsideCachesFound = roadsideCachesFound + if (type == ClusterType.ROADSIDE_CACHE) 1 else 0,
        explorerNestsFound = explorerNestsFound + if (type == ClusterType.EXPLORER_NEST) 1 else 0,
        treasureGardensFound = treasureGardensFound + if (type == ClusterType.TREASURE_GARDEN) 1 else 0,
        ancientVaultsFound = ancientVaultsFound + if (type == ClusterType.ANCIENT_VAULT) 1 else 0,
        legendaryHoardsFound = legendaryHoardsFound + if (type == ClusterType.LEGENDARY_HOARD) 1 else 0,
    )

    fun withCompletion(type: ClusterType, creditsEarned: Int): TreasureClusterStatistics = copy(
        clustersCompleted = clustersCompleted + 1,
        roadsideCachesCompleted = roadsideCachesCompleted +
            if (type == ClusterType.ROADSIDE_CACHE) 1 else 0,
        explorerNestsCompleted = explorerNestsCompleted +
            if (type == ClusterType.EXPLORER_NEST) 1 else 0,
        treasureGardensCompleted = treasureGardensCompleted +
            if (type == ClusterType.TREASURE_GARDEN) 1 else 0,
        ancientVaultsCompleted = ancientVaultsCompleted +
            if (type == ClusterType.ANCIENT_VAULT) 1 else 0,
        legendaryHoardsCompleted = legendaryHoardsCompleted +
            if (type == ClusterType.LEGENDARY_HOARD) 1 else 0,
        totalRewardsEarned = totalRewardsEarned + creditsEarned.coerceAtLeast(0),
    )

    fun completedEntriesOrdered(): List<Pair<ClusterType, Int>> =
        ClusterType.ALL_ORDERED.map { it to completedCountFor(it) }

    companion object {
        val EMPTY = TreasureClusterStatistics()
    }
}
