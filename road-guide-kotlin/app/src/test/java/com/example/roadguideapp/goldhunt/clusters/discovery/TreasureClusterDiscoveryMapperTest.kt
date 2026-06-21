package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureClusterDiscoveryMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesDiscoveryFields() {
        val cluster = TreasureCluster(
            clusterId = "cluster:abc",
            clusterType = ClusterType.TREASURE_GARDEN,
            centerLatitude = 51.5,
            centerLongitude = -0.2,
            radiusMeters = 95.0,
            treasureCount = 6,
            clusterSeed = 99L,
        )
        val entity = TreasureClusterDiscoveryMapper.toEntity(
            cluster = cluster,
            trigger = ClusterDiscoveryTrigger.EXPLORATION,
            discoveredAtMs = 1_700_000_000_000L,
        )
        val domain = requireNotNull(TreasureClusterDiscoveryMapper.toDomain(entity))
        assertEquals(cluster.clusterId, domain.clusterId)
        assertEquals(cluster.clusterType, domain.clusterType)
        assertEquals(1_700_000_000_000L, domain.discoveredAtMs)
        assertEquals(ClusterDiscoveryTrigger.EXPLORATION, domain.discoveryTrigger)
        assertEquals(cluster.centerLatitude, domain.centerLatitude, 0.0)
        assertEquals(cluster.centerLongitude, domain.centerLongitude, 0.0)
        assertEquals(cluster.radiusMeters, domain.radiusMeters, 0.0)
    }
}
