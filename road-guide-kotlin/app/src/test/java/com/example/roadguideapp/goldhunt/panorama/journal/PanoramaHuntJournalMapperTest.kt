package com.example.roadguideapp.goldhunt.panorama.journal

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import org.junit.Assert.assertEquals
import org.junit.Test

class PanoramaHuntJournalMapperTest {
    @Test
    fun resolveStatus_mapsCountsToJournalStatus() {
        assertEquals(PanoramaHuntJournalStatus.MISSING, PanoramaHuntJournalMapper.resolveStatus(0, 0))
        assertEquals(PanoramaHuntJournalStatus.DISCOVERED, PanoramaHuntJournalMapper.resolveStatus(2, 0))
        assertEquals(PanoramaHuntJournalStatus.COMPLETED, PanoramaHuntJournalMapper.resolveStatus(3, 1))
    }

    @Test
    fun buildProgress_countsDiscoveredCompletedAndMissing() {
        val entries = listOf(
            entry(PanoramaHuntType.HIDDEN_SYMBOL, PanoramaHuntJournalStatus.COMPLETED, rewards = 40),
            entry(PanoramaHuntType.OBJECT_HUNT, PanoramaHuntJournalStatus.DISCOVERED),
            entry(PanoramaHuntType.PANORAMA_PUZZLE, PanoramaHuntJournalStatus.MISSING),
            entry(PanoramaHuntType.SECRET_CODE, PanoramaHuntJournalStatus.MISSING),
            entry(PanoramaHuntType.RELIC_HUNT, PanoramaHuntJournalStatus.MISSING),
        )
        val progress = PanoramaHuntJournalMapper.buildProgress(entries)
        assertEquals(2, progress.discoveredCount)
        assertEquals(1, progress.completedCount)
        assertEquals(3, progress.missingCount)
        assertEquals(5, progress.trackableCount)
        assertEquals(40, progress.totalRewardsEarned)
    }

    @Test
    fun toEntity_roundTripsCatalogKey() {
        val entity = PanoramaHuntJournalMapper.toEntity(
            huntType = PanoramaHuntType.SECRET_CODE,
            status = PanoramaHuntJournalStatus.COMPLETED,
            firstDiscoveredAtMs = 100L,
            completedAtMs = 200L,
            totalRewardsEarned = 75,
            totalXpEarned = 120L,
            discoveredCount = 2,
            completedCount = 1,
            achievementKey = "panorama_hunt_secret_code",
            storyFragmentId = "story_fragment_secret_code",
            legendaryRelicKey = null,
            timestampMs = 200L,
        )
        assertEquals("SECRET_CODE", entity.catalogKey)
        assertEquals("COMPLETED", entity.status)
        assertEquals(75, entity.totalRewardsEarned)
        assertEquals(120L, entity.totalXpEarned)
    }

    private fun entry(
        huntType: PanoramaHuntType,
        status: PanoramaHuntJournalStatus,
        rewards: Int = 0,
        xp: Long = 0L,
    ): PanoramaHuntJournalEntry {
        val template = PanoramaHuntJournalCatalog.templateForType(huntType)!!
        return PanoramaHuntJournalMapper.toDomain(
            template = template,
            entity = PanoramaHuntJournalMapper.toEntity(
                huntType = huntType,
                status = status,
                firstDiscoveredAtMs = if (status == PanoramaHuntJournalStatus.MISSING) null else 100L,
                completedAtMs = if (status == PanoramaHuntJournalStatus.COMPLETED) 200L else null,
                totalRewardsEarned = rewards,
                totalXpEarned = xp,
                discoveredCount = if (status == PanoramaHuntJournalStatus.MISSING) 0 else 1,
                completedCount = if (status == PanoramaHuntJournalStatus.COMPLETED) 1 else 0,
                achievementKey = template.achievementKey,
                storyFragmentId = template.storyFragmentKey,
                legendaryRelicKey = template.legendaryRelicKey,
                timestampMs = 200L,
            ),
        )
    }
}
