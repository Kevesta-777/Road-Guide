package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import org.junit.Assert.assertEquals
import org.junit.Test

class ClusterEncyclopediaMapperTest {
    private val template = ClusterEncyclopediaCatalog.templateForType(ClusterType.ROADSIDE_CACHE)!!

    @Test
    fun toDomain_completedEntry_mapsFields() {
        val entity = ClusterEncyclopediaEntryEntity(
            catalogKey = ClusterType.ROADSIDE_CACHE.id,
            status = ClusterEncyclopediaStatus.COMPLETED.id,
            firstDiscoveredAtMs = 1_000L,
            completedAtMs = 2_000L,
            totalRewardsEarned = 75,
            discoveredCount = 3,
            completedCount = 1,
            achievementKey = "cluster_encyclopedia:ROADSIDE_CACHE:completed",
            storyFragmentId = "story:roadsie_cache",
        )
        val entry = ClusterEncyclopediaMapper.toDomain(template, entity)
        assertEquals(ClusterEncyclopediaStatus.COMPLETED, entry.status)
        assertEquals(1_000L, entry.firstDiscoveredAtMs)
        assertEquals(2_000L, entry.completedAtMs)
        assertEquals(75, entry.totalRewardsEarned)
        assertEquals(3, entry.discoveredCount)
        assertEquals(1, entry.completedCount)
    }

    @Test
    fun toDomain_missingEntry_usesTemplateDefaults() {
        val entry = ClusterEncyclopediaMapper.toDomain(template, null)
        assertEquals(ClusterEncyclopediaStatus.MISSING, entry.status)
        assertEquals(0, entry.discoveredCount)
        assertEquals(0, entry.completedCount)
        assertEquals(0, entry.totalRewardsEarned)
        assertEquals(template.achievementKey, entry.achievementKey)
        assertEquals(template.storyFragmentKey, entry.storyFragmentKey)
    }

    @Test
    fun resolveStatus_transitionsCorrectly() {
        assertEquals(ClusterEncyclopediaStatus.MISSING, ClusterEncyclopediaMapper.resolveStatus(0, 0))
        assertEquals(ClusterEncyclopediaStatus.DISCOVERED, ClusterEncyclopediaMapper.resolveStatus(2, 0))
        assertEquals(ClusterEncyclopediaStatus.COMPLETED, ClusterEncyclopediaMapper.resolveStatus(1, 1))
        assertEquals(ClusterEncyclopediaStatus.COMPLETED, ClusterEncyclopediaMapper.resolveStatus(3, 2))
    }

    @Test
    fun buildProgress_countsDiscoveredCompletedAndRewards() {
        val entries = listOf(
            ClusterEncyclopediaMapper.toDomain(
                template,
                ClusterEncyclopediaMapper.toEntity(
                    clusterType = ClusterType.ROADSIDE_CACHE,
                    status = ClusterEncyclopediaStatus.COMPLETED,
                    firstDiscoveredAtMs = 1L,
                    completedAtMs = 2L,
                    totalRewardsEarned = 50,
                    discoveredCount = 2,
                    completedCount = 1,
                    achievementKey = null,
                    storyFragmentId = null,
                    timestampMs = 2L,
                ),
            ),
            ClusterEncyclopediaMapper.toDomain(
                ClusterEncyclopediaCatalog.templateForType(ClusterType.EXPLORER_NEST)!!,
                ClusterEncyclopediaMapper.toEntity(
                    clusterType = ClusterType.EXPLORER_NEST,
                    status = ClusterEncyclopediaStatus.DISCOVERED,
                    firstDiscoveredAtMs = 3L,
                    completedAtMs = null,
                    totalRewardsEarned = 0,
                    discoveredCount = 1,
                    completedCount = 0,
                    achievementKey = null,
                    storyFragmentId = null,
                    timestampMs = 3L,
                ),
            ),
            ClusterEncyclopediaMapper.toDomain(
                ClusterEncyclopediaCatalog.templateForType(ClusterType.TREASURE_GARDEN)!!,
                null,
            ),
        )
        val progress = ClusterEncyclopediaMapper.buildProgress(entries)
        assertEquals(2, progress.discoveredCount)
        assertEquals(1, progress.completedCount)
        assertEquals(3, progress.trackableCount)
        assertEquals(50, progress.totalRewardsEarned)
        assertEquals(2f / 3f, progress.discoveredFraction, 0.001f)
        assertEquals(1f / 3f, progress.completionFraction, 0.001f)
    }
}
