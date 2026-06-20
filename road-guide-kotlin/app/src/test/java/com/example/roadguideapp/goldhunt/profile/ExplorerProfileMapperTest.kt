package com.example.roadguideapp.goldhunt.profile

import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatistics
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatistics
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntStatistics
import com.example.roadguideapp.goldhunt.radar.statistics.RadarStatistics
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.statistics.SecretPlaceCategoryStatistics
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatistics
import org.junit.Assert.assertEquals
import org.junit.Test

class ExplorerProfileMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesFields() {
        val domain = ExplorerProfile(
            explorerLevel = 3,
            currentXp = 1250L,
            totalCredits = 42,
            totalRoadsDiscovered = 7,
            totalAreasDiscovered = 5,
            totalTreasuresCollected = 18,
            totalSecretPlacesFound = 2,
            totalClustersDiscovered = 4,
            totalClustersCompleted = 2,
            totalPanoramaHuntsCompleted = 1,
            totalDistanceExploredM = 12_345.6,
            panoramaHuntStatistics = PanoramaHuntStatistics(
                totalHunts = 2,
                huntsCompleted = 1,
                targetsFound = 3,
                rewardsEarned = 50,
                xpEarned = 80L,
                hiddenSymbolsFound = 1,
            ),
            radarStatistics = RadarStatistics(
                totalScans = 10,
                successfulScans = 6,
                treasuresFound = 4,
                clustersFound = 2,
                secretPlacesFound = 1,
                storyDiscoveries = 3,
                legendaryDiscoveries = 1,
            ),
            clusterStatistics = TreasureClusterStatistics(
                clustersFound = 4,
                clustersCompleted = 2,
                roadsideCachesFound = 1,
                explorerNestsFound = 1,
                treasureGardensFound = 1,
                ancientVaultsFound = 1,
                legendaryHoardsFound = 0,
                roadsideCachesCompleted = 1,
                explorerNestsCompleted = 0,
                treasureGardensCompleted = 1,
                ancientVaultsCompleted = 1,
                legendaryHoardsCompleted = 0,
                totalRewardsEarned = 175,
            ),
            secretPlaceCategoryStatistics = SecretPlaceCategoryStatistics(
                discoveredCount = 3,
                completedCount = 1,
                creditsEarned = 45,
                xpEarned = 175L,
                bestCategory = SecretPlaceCategory.MYSTERY_ZONE,
                rarestCategory = SecretPlaceCategory.LEGENDARY_SITE,
                mysteryZonesFound = 2,
                legendarySitesFound = 1,
                legendarySitesCompleted = 1,
            ),
            treasureRarityStats = TreasureRarityStatistics(
                commonFound = 10,
                uncommonFound = 4,
                rareFound = 2,
                epicFound = 1,
                legendaryFound = 0,
                mythicFound = 1,
            ),
            achievementStatistics = AchievementStatistics.EMPTY.withCatalogSize(
                AchievementRegistry.count(),
            ),
            activeAchievementTitleKey = "title_achievement_streak",
            schemaVersion = ExplorerProfileSchema.VERSION,
            extensionJson = """{"achievements":[]}""",
            createdAtMs = 100L,
            updatedAtMs = 200L,
        )
        val roundTrip = ExplorerProfileMapper.toDomain(ExplorerProfileMapper.toEntity(domain))
        assertEquals(domain, roundTrip)
    }
}
