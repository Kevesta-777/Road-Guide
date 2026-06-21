package com.example.roadguideapp.goldhunt.clusters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TreasureClusterTest {
    @Test
    fun remainingCount_derivesFromTreasureAndCollected() {
        val cluster = sampleCluster(collectedCount = 2, treasureCount = 5)
        assertEquals(3, cluster.remainingCount)
    }

    @Test
    fun isComplete_whenAllTreasuresCollected() {
        val cluster = sampleCluster(collectedCount = 4, treasureCount = 4, discovered = true)
        assertTrue(cluster.isComplete)
        assertEquals(0, cluster.remainingCount)
    }

    @Test
    fun resolvedHooks_fallBackToClusterType() {
        val cluster = sampleCluster(
            clusterType = ClusterType.ANCIENT_VAULT,
            collectedCount = 0,
        )
        assertEquals("story_fragment_ancient_vault", cluster.resolvedStoryFragmentId)
        assertEquals("cluster_ancient_vault", cluster.resolvedAchievementKey)
    }

    @Test
    fun instanceHooks_overrideClusterTypeDefaults() {
        val cluster = sampleCluster(
            clusterType = ClusterType.ROADSIDE_CACHE,
            storyFragmentId = "story_custom",
            achievementKey = "achievement_custom",
            secretPlaceId = "sp:1",
            legendaryRelicKey = "relic_alpha",
        )
        assertEquals("story_custom", cluster.resolvedStoryFragmentId)
        assertEquals("achievement_custom", cluster.resolvedAchievementKey)
        assertTrue(cluster.hasSecretPlaceLink)
        assertTrue(cluster.hasLegendaryRelicHook)
    }

    @Test
    fun incrementCollectedCount_capsAtTreasureCount() {
        val cluster = sampleCluster(collectedCount = 2, treasureCount = 3)
        val next = cluster.incrementCollectedCount()
        assertEquals(3, next.collectedCount)
        assertTrue(next.isComplete)
        assertEquals(next, next.incrementCollectedCount())
    }

    @Test
    fun clusterId_forL1Anchor_isStable() {
        val id = TreasureClusterId.forL1Anchor(
            playRegionId = "london",
            regionL1Id = "L1:12:34",
            clusterSeed = 99L,
        )
        assertTrue(id.startsWith("tc:v${ClusterSchema.VERSION}:london:L1:12:34:seed99"))
        assertNotNull(ClusterType.fromId(ClusterType.EXPLORER_NEST.id))
    }

    private fun sampleCluster(
        clusterType: ClusterType = ClusterType.EXPLORER_NEST,
        collectedCount: Int = 0,
        treasureCount: Int = 4,
        discovered: Boolean = false,
        secretPlaceId: String? = null,
        storyFragmentId: String? = null,
        achievementKey: String? = null,
        legendaryRelicKey: String? = null,
    ): TreasureCluster = TreasureCluster(
        clusterId = "tc:test:1",
        clusterType = clusterType,
        centerLatitude = 51.5074,
        centerLongitude = -0.1278,
        radiusMeters = 120.0,
        treasureCount = treasureCount,
        clusterSeed = 42L,
        discovered = discovered,
        collectedCount = collectedCount,
        secretPlaceId = secretPlaceId,
        storyFragmentId = storyFragmentId,
        achievementKey = achievementKey,
        legendaryRelicKey = legendaryRelicKey,
    )
}
