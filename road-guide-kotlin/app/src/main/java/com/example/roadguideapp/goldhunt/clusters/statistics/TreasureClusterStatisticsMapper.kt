package com.example.roadguideapp.goldhunt.clusters.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfile
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal object TreasureClusterStatisticsMapper {
    fun fromProfile(profile: ExplorerProfile): TreasureClusterStatistics =
        profile.clusterStatistics

    fun fromEntity(entity: ExplorerProfileEntity): TreasureClusterStatistics = TreasureClusterStatistics(
        clustersFound = entity.totalClustersDiscovered,
        clustersCompleted = entity.totalClustersCompleted,
        roadsideCachesFound = entity.clusterStatsRoadsideCachesFound,
        explorerNestsFound = entity.clusterStatsExplorerNestsFound,
        treasureGardensFound = entity.clusterStatsTreasureGardensFound,
        ancientVaultsFound = entity.clusterStatsAncientVaultsFound,
        legendaryHoardsFound = entity.clusterStatsLegendaryHoardsFound,
        roadsideCachesCompleted = entity.clusterStatsRoadsideCachesCompleted,
        explorerNestsCompleted = entity.clusterStatsExplorerNestsCompleted,
        treasureGardensCompleted = entity.clusterStatsTreasureGardensCompleted,
        ancientVaultsCompleted = entity.clusterStatsAncientVaultsCompleted,
        legendaryHoardsCompleted = entity.clusterStatsLegendaryHoardsCompleted,
        totalRewardsEarned = entity.totalClusterRewardsEarned,
    )

    fun applyToProfile(
        profile: ExplorerProfile,
        stats: TreasureClusterStatistics,
    ): ExplorerProfile = profile.copy(
        totalClustersDiscovered = stats.clustersFound,
        totalClustersCompleted = stats.clustersCompleted,
        clusterStatistics = stats,
    )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: TreasureClusterStatistics,
    ): ExplorerProfileEntity = entity.copy(
        totalClustersDiscovered = stats.clustersFound,
        totalClustersCompleted = stats.clustersCompleted,
        clusterStatsRoadsideCachesFound = stats.roadsideCachesFound,
        clusterStatsExplorerNestsFound = stats.explorerNestsFound,
        clusterStatsTreasureGardensFound = stats.treasureGardensFound,
        clusterStatsAncientVaultsFound = stats.ancientVaultsFound,
        clusterStatsLegendaryHoardsFound = stats.legendaryHoardsFound,
        clusterStatsRoadsideCachesCompleted = stats.roadsideCachesCompleted,
        clusterStatsExplorerNestsCompleted = stats.explorerNestsCompleted,
        clusterStatsTreasureGardensCompleted = stats.treasureGardensCompleted,
        clusterStatsAncientVaultsCompleted = stats.ancientVaultsCompleted,
        clusterStatsLegendaryHoardsCompleted = stats.legendaryHoardsCompleted,
        totalClusterRewardsEarned = stats.totalRewardsEarned,
    )
}
