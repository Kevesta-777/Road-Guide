package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class SecretPlaceCollectionBookMapperTest {
    private val template =
        SecretPlaceCollectionBookCatalog.templateForCategory(SecretPlaceCategory.HISTORIC_LANDMARK)!!

    @Test
    fun toDomain_completedEntry_mapsFields() {
        val entity = SecretPlaceCollectionBookEntryEntity(
            catalogKey = SecretPlaceCategory.HISTORIC_LANDMARK.id,
            status = SecretPlaceCollectionBookStatus.COMPLETED.id,
            firstDiscoveredAtMs = 1_000L,
            completedAtMs = 2_000L,
            totalCreditsEarned = 120,
            totalXpEarned = 450L,
            discoveredCount = 2,
            completedCount = 1,
            minimumExplorerLevel = 3,
            achievementKey = "secret_place_historic_landmark",
            storyFragmentId = "story_fragment_historic_landmark",
        )
        val entry = SecretPlaceCollectionBookMapper.toDomain(template, entity)
        assertEquals(SecretPlaceCollectionBookStatus.COMPLETED, entry.status)
        assertEquals(1_000L, entry.firstDiscoveredAtMs)
        assertEquals(2_000L, entry.completedAtMs)
        assertEquals(120, entry.totalCreditsEarned)
        assertEquals(450L, entry.totalXpEarned)
        assertEquals(2, entry.discoveredCount)
        assertEquals(1, entry.completedCount)
        assertEquals(3, entry.minimumExplorerLevel)
        assertEquals("secret_place_historic_landmark", entry.achievementKey)
        assertEquals("story_fragment_historic_landmark", entry.storyFragmentKey)
    }

    @Test
    fun toDomain_missingEntry_usesTemplateDefaults() {
        val entry = SecretPlaceCollectionBookMapper.toDomain(template, null)
        assertEquals(SecretPlaceCollectionBookStatus.MISSING, entry.status)
        assertEquals(0, entry.discoveredCount)
        assertEquals(0, entry.completedCount)
        assertEquals(0, entry.totalCreditsEarned)
        assertEquals(0L, entry.totalXpEarned)
        assertEquals(template.achievementKey, entry.achievementKey)
        assertEquals(template.storyFragmentKey, entry.storyFragmentKey)
    }

    @Test
    fun resolveStatus_transitionsCorrectly() {
        assertEquals(
            SecretPlaceCollectionBookStatus.MISSING,
            SecretPlaceCollectionBookMapper.resolveStatus(0, 0),
        )
        assertEquals(
            SecretPlaceCollectionBookStatus.DISCOVERED,
            SecretPlaceCollectionBookMapper.resolveStatus(2, 0),
        )
        assertEquals(
            SecretPlaceCollectionBookStatus.COMPLETED,
            SecretPlaceCollectionBookMapper.resolveStatus(1, 1),
        )
        assertEquals(
            SecretPlaceCollectionBookStatus.COMPLETED,
            SecretPlaceCollectionBookMapper.resolveStatus(3, 2),
        )
    }

    @Test
    fun buildProgress_countsDiscoveredCompletedMissingAndRewards() {
        val entries = listOf(
            SecretPlaceCollectionBookMapper.toDomain(
                template,
                SecretPlaceCollectionBookMapper.toEntity(
                    category = SecretPlaceCategory.HISTORIC_LANDMARK,
                    status = SecretPlaceCollectionBookStatus.COMPLETED,
                    firstDiscoveredAtMs = 1L,
                    completedAtMs = 2L,
                    totalCreditsEarned = 80,
                    totalXpEarned = 200L,
                    discoveredCount = 2,
                    completedCount = 1,
                    minimumExplorerLevel = 3,
                    achievementKey = null,
                    storyFragmentId = null,
                    timestampMs = 2L,
                ),
            ),
            SecretPlaceCollectionBookMapper.toDomain(
                SecretPlaceCollectionBookCatalog.templateForCategory(SecretPlaceCategory.MYSTERY_ZONE)!!,
                SecretPlaceCollectionBookMapper.toEntity(
                    category = SecretPlaceCategory.MYSTERY_ZONE,
                    status = SecretPlaceCollectionBookStatus.DISCOVERED,
                    firstDiscoveredAtMs = 3L,
                    completedAtMs = null,
                    totalCreditsEarned = 0,
                    totalXpEarned = 0L,
                    discoveredCount = 1,
                    completedCount = 0,
                    minimumExplorerLevel = 4,
                    achievementKey = null,
                    storyFragmentId = null,
                    timestampMs = 3L,
                ),
            ),
            SecretPlaceCollectionBookMapper.toDomain(
                SecretPlaceCollectionBookCatalog.templateForCategory(SecretPlaceCategory.NATURE_SANCTUARY)!!,
                null,
            ),
        )
        val progress = SecretPlaceCollectionBookMapper.buildProgress(entries)
        assertEquals(2, progress.discoveredCount)
        assertEquals(1, progress.completedCount)
        assertEquals(1, progress.missingCount)
        assertEquals(3, progress.trackableCount)
        assertEquals(80, progress.totalCreditsEarned)
        assertEquals(200L, progress.totalXpEarned)
        assertEquals(2f / 3f, progress.discoveredFraction, 0.001f)
        assertEquals(1f / 3f, progress.completionFraction, 0.001f)
    }
}
