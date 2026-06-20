package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

internal object SecretPlaceCollectionBookMapper {
    fun toDomain(
        template: SecretPlaceCollectionBookTemplate,
        entity: SecretPlaceCollectionBookEntryEntity?,
    ): SecretPlaceCollectionBookEntry {
        val status = entity?.status?.let(SecretPlaceCollectionBookStatus::fromId)
            ?: SecretPlaceCollectionBookCatalog.initialStatus()
        return SecretPlaceCollectionBookEntry(
            category = template.category,
            displayName = template.displayName,
            status = status,
            firstDiscoveredAtMs = entity?.firstDiscoveredAtMs,
            completedAtMs = entity?.completedAtMs,
            totalCreditsEarned = entity?.totalCreditsEarned ?: 0,
            totalXpEarned = entity?.totalXpEarned ?: 0L,
            discoveredCount = entity?.discoveredCount ?: 0,
            completedCount = entity?.completedCount ?: 0,
            minimumExplorerLevel = entity?.minimumExplorerLevel ?: template.minimumExplorerLevel,
            achievementKey = entity?.achievementKey ?: template.achievementKey,
            storyFragmentKey = entity?.storyFragmentId ?: template.storyFragmentKey,
            schemaVersion = entity?.schemaVersion ?: SecretPlaceCollectionBookSchema.VERSION,
            extensionJson = entity?.extensionJson ?: SecretPlaceCollectionBookSchema.EMPTY_EXTENSIONS_JSON,
        )
    }

    fun toEntity(
        category: SecretPlaceCategory,
        status: SecretPlaceCollectionBookStatus,
        firstDiscoveredAtMs: Long?,
        completedAtMs: Long?,
        totalCreditsEarned: Int,
        totalXpEarned: Long,
        discoveredCount: Int,
        completedCount: Int,
        minimumExplorerLevel: Int,
        achievementKey: String?,
        storyFragmentId: String?,
        timestampMs: Long,
    ): SecretPlaceCollectionBookEntryEntity = SecretPlaceCollectionBookEntryEntity(
        catalogKey = category.id,
        status = status.id,
        firstDiscoveredAtMs = firstDiscoveredAtMs,
        completedAtMs = completedAtMs,
        totalCreditsEarned = totalCreditsEarned,
        totalXpEarned = totalXpEarned,
        discoveredCount = discoveredCount,
        completedCount = completedCount,
        minimumExplorerLevel = minimumExplorerLevel,
        achievementKey = achievementKey,
        storyFragmentId = storyFragmentId,
        updatedAtMs = timestampMs,
    )

    fun resolveStatus(discoveredCount: Int, completedCount: Int): SecretPlaceCollectionBookStatus = when {
        discoveredCount <= 0 -> SecretPlaceCollectionBookStatus.MISSING
        completedCount > 0 -> SecretPlaceCollectionBookStatus.COMPLETED
        else -> SecretPlaceCollectionBookStatus.DISCOVERED
    }

    fun buildProgress(entries: List<SecretPlaceCollectionBookEntry>): SecretPlaceCollectionBookProgress {
        val trackable = entries.size
        val discovered = entries.count { !it.isMissing }
        val completed = entries.count { it.isCompleted }
        val missing = entries.count { it.isMissing }
        return SecretPlaceCollectionBookProgress(
            discoveredCount = discovered,
            completedCount = completed,
            missingCount = missing,
            trackableCount = trackable,
            totalCreditsEarned = entries.sumOf { it.totalCreditsEarned },
            totalXpEarned = entries.sumOf { it.totalXpEarned },
        )
    }
}
