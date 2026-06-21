package com.example.roadguideapp.goldhunt.events.collectionbook

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEventCollectionBookMapperTest {
    private val eventType = SeasonalEventType.SPRING_BLOSSOM
    private val template = SeasonalEventCollectionBookCatalog.templateFor(eventType)!!

    @Test
    fun toDomain_completedEntry_mapsFieldsAndCountsAchievements() {
        val entity = SeasonalEventCollectionBookMapper.toEntity(
            eventId = "spring_blossom_2026",
            eventType = eventType,
            cycleYear = 2026,
            displayName = eventType.displayName,
            status = SeasonalEventCollectionBookStatus.COMPLETED,
            firstParticipatedAtMs = 1_000L,
            completedAtMs = 2_000L,
            totalCreditsEarned = 120,
            totalXpEarned = 450L,
            fragmentsEarned = 2,
            participationAchievementKey = template.participationAchievementKey,
            completionAchievementKey = template.completionAchievementKey,
            masteryAchievementKey = template.masteryAchievementKey,
            storyFragmentKey = template.storyFragmentKey,
            legendaryRelicKey = template.legendaryRelicKey,
            timestampMs = 2_000L,
        )
        val entry = SeasonalEventCollectionBookMapper.toDomain(
            entity = entity,
            participationAchievementEarned = true,
            completionAchievementEarned = true,
            masteryAchievementEarned = false,
            storyFragmentEarned = true,
            legendaryRelicEarned = false,
        )!!

        assertEquals(SeasonalEventCollectionBookStatus.COMPLETED, entry.status)
        assertEquals(1_000L, entry.firstParticipatedAtMs)
        assertEquals(2_000L, entry.completedAtMs)
        assertEquals(120, entry.totalCreditsEarned)
        assertEquals(450L, entry.totalXpEarned)
        assertEquals(2, entry.fragmentsEarned)
        assertEquals(3, entry.achievementsEarned)
        assertTrue(entry.participationAchievementEarned)
        assertTrue(entry.completionAchievementEarned)
        assertFalse(entry.masteryAchievementEarned)
        assertTrue(entry.storyFragmentEarned)
        assertFalse(entry.legendaryRelicEarned)
    }

    @Test
    fun resolveStatus_transitionsCorrectly() {
        assertEquals(
            SeasonalEventCollectionBookStatus.MISSING,
            SeasonalEventCollectionBookMapper.resolveStatus(isCompleted = false, treasuresCollected = 0),
        )
        assertEquals(
            SeasonalEventCollectionBookStatus.PARTICIPATED,
            SeasonalEventCollectionBookMapper.resolveStatus(isCompleted = false, treasuresCollected = 1),
        )
        assertEquals(
            SeasonalEventCollectionBookStatus.COMPLETED,
            SeasonalEventCollectionBookMapper.resolveStatus(isCompleted = true, treasuresCollected = 0),
        )
    }

    @Test
    fun buildProgress_countsFamiliesCyclesAndRewards() {
        val springEntry = SeasonalEventCollectionBookMapper.toDomain(
            entity = SeasonalEventCollectionBookMapper.toEntity(
                eventId = "spring_blossom_2026",
                eventType = SeasonalEventType.SPRING_BLOSSOM,
                cycleYear = 2026,
                displayName = SeasonalEventType.SPRING_BLOSSOM.displayName,
                status = SeasonalEventCollectionBookStatus.COMPLETED,
                firstParticipatedAtMs = 1L,
                completedAtMs = 2L,
                totalCreditsEarned = 80,
                totalXpEarned = 200L,
                fragmentsEarned = 1,
                participationAchievementKey = null,
                completionAchievementKey = null,
                masteryAchievementKey = null,
                storyFragmentKey = null,
                legendaryRelicKey = null,
                timestampMs = 2L,
            ),
            participationAchievementEarned = true,
            completionAchievementEarned = true,
            masteryAchievementEarned = false,
            storyFragmentEarned = false,
            legendaryRelicEarned = false,
        )!!
        val summerEntry = SeasonalEventCollectionBookMapper.toDomain(
            entity = SeasonalEventCollectionBookMapper.toEntity(
                eventId = "summer_explorer_2026",
                eventType = SeasonalEventType.SUMMER_EXPLORER,
                cycleYear = 2026,
                displayName = SeasonalEventType.SUMMER_EXPLORER.displayName,
                status = SeasonalEventCollectionBookStatus.PARTICIPATED,
                firstParticipatedAtMs = 3L,
                completedAtMs = null,
                totalCreditsEarned = 0,
                totalXpEarned = 0L,
                fragmentsEarned = 0,
                participationAchievementKey = null,
                completionAchievementKey = null,
                masteryAchievementKey = null,
                storyFragmentKey = null,
                legendaryRelicKey = null,
                timestampMs = 3L,
            ),
            participationAchievementEarned = true,
            completionAchievementEarned = false,
            masteryAchievementEarned = false,
            storyFragmentEarned = false,
            legendaryRelicEarned = false,
        )!!
        val missingFamilies = listOf(
            SeasonalEventCollectionBookFamilyEntry(
                eventType = SeasonalEventType.HALLOWEEN_MYSTERY,
                displayName = SeasonalEventType.HALLOWEEN_MYSTERY.displayName,
                minExplorerLevel = SeasonalEventType.HALLOWEEN_MYSTERY.minExplorerLevel,
                participationAchievementKey = "participation",
                completionAchievementKey = "completion",
                masteryAchievementKey = "mastery",
                storyFragmentKey = "story",
                legendaryRelicKey = "relic",
            ),
        )

        val progress = SeasonalEventCollectionBookMapper.buildProgress(
            cycleEntries = listOf(springEntry, summerEntry),
            missingFamilies = missingFamilies,
        )

        assertEquals(2, progress.participatedCycles)
        assertEquals(1, progress.completedCycles)
        assertEquals(2, progress.familiesParticipated)
        assertEquals(1, progress.familiesCompleted)
        assertEquals(1, progress.familiesMissing)
        assertEquals(SeasonalEventCollectionBookCatalog.displayTemplates.size, progress.trackableFamilies)
        assertEquals(80, progress.totalCreditsEarned)
        assertEquals(200L, progress.totalXpEarned)
        assertEquals(1, progress.totalFragmentsEarned)
        assertEquals(3, progress.totalAchievementsEarned)
        assertEquals(
            2f / progress.trackableFamilies.toFloat(),
            progress.participatedFraction,
            0.001f,
        )
        assertEquals(
            1f / progress.trackableFamilies.toFloat(),
            progress.completedFraction,
            0.001f,
        )
    }
}
