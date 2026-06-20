package com.example.roadguideapp.goldhunt.panorama.journal

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

internal object PanoramaHuntJournalMapper {
    fun toDomain(
        template: PanoramaHuntJournalTemplate,
        entity: PanoramaHuntJournalEntryEntity?,
        achievementUnlocked: Boolean = false,
        storyFragmentUnlocked: Boolean = false,
        legendaryRelicUnlocked: Boolean = false,
    ): PanoramaHuntJournalEntry {
        val status = entity?.status?.let(PanoramaHuntJournalStatus::fromId)
            ?: PanoramaHuntJournalCatalog.initialStatus()
        return PanoramaHuntJournalEntry(
            huntType = template.huntType,
            displayName = template.displayName,
            status = status,
            firstDiscoveredAtMs = entity?.firstDiscoveredAtMs,
            completedAtMs = entity?.completedAtMs,
            totalRewardsEarned = entity?.totalRewardsEarned ?: 0,
            totalXpEarned = entity?.totalXpEarned ?: 0L,
            discoveredCount = entity?.discoveredCount ?: 0,
            completedCount = entity?.completedCount ?: 0,
            achievementKey = entity?.achievementKey ?: template.achievementKey,
            storyFragmentKey = entity?.storyFragmentId ?: template.storyFragmentKey,
            legendaryRelicKey = entity?.legendaryRelicKey ?: template.legendaryRelicKey,
            achievementUnlocked = achievementUnlocked,
            storyFragmentUnlocked = storyFragmentUnlocked,
            legendaryRelicUnlocked = legendaryRelicUnlocked,
            schemaVersion = entity?.schemaVersion ?: PanoramaHuntJournalSchema.VERSION,
            extensionJson = entity?.extensionJson ?: PanoramaHuntJournalSchema.EMPTY_EXTENSIONS_JSON,
        )
    }

    fun toEntity(
        huntType: PanoramaHuntType,
        status: PanoramaHuntJournalStatus,
        firstDiscoveredAtMs: Long?,
        completedAtMs: Long?,
        totalRewardsEarned: Int,
        totalXpEarned: Long,
        discoveredCount: Int,
        completedCount: Int,
        achievementKey: String?,
        storyFragmentId: String?,
        legendaryRelicKey: String?,
        timestampMs: Long,
    ): PanoramaHuntJournalEntryEntity = PanoramaHuntJournalEntryEntity(
        catalogKey = huntType.id,
        status = status.id,
        firstDiscoveredAtMs = firstDiscoveredAtMs,
        completedAtMs = completedAtMs,
        totalRewardsEarned = totalRewardsEarned,
        totalXpEarned = totalXpEarned,
        discoveredCount = discoveredCount,
        completedCount = completedCount,
        achievementKey = achievementKey,
        storyFragmentId = storyFragmentId,
        legendaryRelicKey = legendaryRelicKey,
        updatedAtMs = timestampMs,
    )

    fun resolveStatus(discoveredCount: Int, completedCount: Int): PanoramaHuntJournalStatus = when {
        discoveredCount <= 0 -> PanoramaHuntJournalStatus.MISSING
        completedCount > 0 -> PanoramaHuntJournalStatus.COMPLETED
        else -> PanoramaHuntJournalStatus.DISCOVERED
    }

    fun buildProgress(entries: List<PanoramaHuntJournalEntry>): PanoramaHuntJournalProgress {
        val trackable = entries.size
        val discovered = entries.count { !it.isMissing }
        val completed = entries.count { it.isCompleted }
        val missing = entries.count { it.isMissing }
        return PanoramaHuntJournalProgress(
            discoveredCount = discovered,
            completedCount = completed,
            missingCount = missing,
            trackableCount = trackable,
            totalRewardsEarned = entries.sumOf { it.totalRewardsEarned },
            totalXpEarned = entries.sumOf { it.totalXpEarned },
        )
    }
}
