package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureClusterCompletionMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesCompletionFields() {
        val cluster = TreasureCluster(
            clusterId = "tc:v1:test:L1:1:1:seed9",
            clusterType = ClusterType.ANCIENT_VAULT,
            centerLatitude = 51.5,
            centerLongitude = -0.2,
            radiusMeters = 120.0,
            treasureCount = 6,
            clusterSeed = 9L,
            legendaryRelicKey = "relic_test",
        )
        val entity = TreasureClusterCompletionMapper.toEntity(
            cluster = cluster,
            creditsGranted = 150,
            xpGranted = 350L,
            completedAtMs = 1_700_000_000_000L,
        )
        val domain = requireNotNull(TreasureClusterCompletionMapper.toDomain(entity))
        assertEquals(cluster.clusterId, domain.clusterId)
        assertEquals(cluster.clusterType, domain.clusterType)
        assertEquals(150, domain.creditsGranted)
        assertEquals(350L, domain.xpGranted)
        assertEquals(cluster.resolvedAchievementKey, domain.achievementKey)
        assertEquals(cluster.resolvedStoryFragmentId, domain.storyFragmentId)
        assertEquals("relic_test", domain.legendaryRelicKey)
    }
}
