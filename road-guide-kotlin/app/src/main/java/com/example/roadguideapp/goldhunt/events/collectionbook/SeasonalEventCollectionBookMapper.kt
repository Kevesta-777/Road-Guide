package com.example.roadguideapp.goldhunt.events.collectionbook

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCatalog
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementTier

internal object SeasonalEventCollectionBookMapper {
    fun toDomain(
        entity: SeasonalEventCollectionBookEntryEntity,
        participationAchievementEarned: Boolean,
        completionAchievementEarned: Boolean,
        masteryAchievementEarned: Boolean,
        storyFragmentEarned: Boolean,
        legendaryRelicEarned: Boolean,
    ): SeasonalEventCollectionBookEntry? {
        val eventType = SeasonalEventType.fromId(entity.eventType) ?: return null
        val status = SeasonalEventCollectionBookStatus.fromId(entity.status)
            ?: SeasonalEventCollectionBookStatus.PARTICIPATED
        val achievementsEarned = countAchievementsEarned(
            participationAchievementEarned = participationAchievementEarned,
            completionAchievementEarned = completionAchievementEarned,
            masteryAchievementEarned = masteryAchievementEarned,
            storyFragmentEarned = storyFragmentEarned,
            legendaryRelicEarned = legendaryRelicEarned,
        )
        return SeasonalEventCollectionBookEntry(
            eventId = entity.eventId,
            eventType = eventType,
            cycleYear = entity.cycleYear,
            displayName = entity.displayName,
            status = status,
            firstParticipatedAtMs = entity.firstParticipatedAtMs,
            completedAtMs = entity.completedAtMs,
            totalCreditsEarned = entity.totalCreditsEarned.coerceAtLeast(0),
            totalXpEarned = entity.totalXpEarned.coerceAtLeast(0L),
            fragmentsEarned = entity.fragmentsEarned.coerceAtLeast(0),
            achievementsEarned = achievementsEarned,
            participationAchievementKey = entity.participationAchievementKey,
            completionAchievementKey = entity.completionAchievementKey,
            masteryAchievementKey = entity.masteryAchievementKey,
            storyFragmentKey = entity.storyFragmentKey,
            legendaryRelicKey = entity.legendaryRelicKey,
            participationAchievementEarned = participationAchievementEarned,
            completionAchievementEarned = completionAchievementEarned,
            masteryAchievementEarned = masteryAchievementEarned,
            storyFragmentEarned = storyFragmentEarned,
            legendaryRelicEarned = legendaryRelicEarned,
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
        )
    }

    fun toEntity(
        eventId: String,
        eventType: SeasonalEventType,
        cycleYear: Int,
        displayName: String,
        status: SeasonalEventCollectionBookStatus,
        firstParticipatedAtMs: Long?,
        completedAtMs: Long?,
        totalCreditsEarned: Int,
        totalXpEarned: Long,
        fragmentsEarned: Int,
        participationAchievementKey: String?,
        completionAchievementKey: String?,
        masteryAchievementKey: String?,
        storyFragmentKey: String?,
        legendaryRelicKey: String?,
        timestampMs: Long,
    ): SeasonalEventCollectionBookEntryEntity = SeasonalEventCollectionBookEntryEntity(
        eventId = eventId,
        eventType = eventType.id,
        cycleYear = cycleYear,
        displayName = displayName,
        status = status.id,
        firstParticipatedAtMs = firstParticipatedAtMs,
        completedAtMs = completedAtMs,
        totalCreditsEarned = totalCreditsEarned.coerceAtLeast(0),
        totalXpEarned = totalXpEarned.coerceAtLeast(0L),
        fragmentsEarned = fragmentsEarned.coerceAtLeast(0),
        achievementsEarned = 0,
        participationAchievementKey = participationAchievementKey,
        completionAchievementKey = completionAchievementKey,
        masteryAchievementKey = masteryAchievementKey,
        storyFragmentKey = storyFragmentKey,
        legendaryRelicKey = legendaryRelicKey,
        updatedAtMs = timestampMs,
    )

    fun resolveStatus(isCompleted: Boolean, treasuresCollected: Int): SeasonalEventCollectionBookStatus =
        when {
            isCompleted -> SeasonalEventCollectionBookStatus.COMPLETED
            treasuresCollected > 0 -> SeasonalEventCollectionBookStatus.PARTICIPATED
            else -> SeasonalEventCollectionBookStatus.MISSING
        }

    fun isMasteryEarned(eventType: SeasonalEventType, masteryCount: Int): Boolean {
        val definition = SeasonalEventAchievementCatalog.eventAchievements(eventType)
            .firstOrNull { it.tier == SeasonalEventAchievementTier.MASTERY }
            ?: return false
        return masteryCount >= definition.targetCount
    }

    fun buildProgress(
        cycleEntries: List<SeasonalEventCollectionBookEntry>,
        missingFamilies: List<SeasonalEventCollectionBookFamilyEntry>,
    ): SeasonalEventCollectionBookProgress {
        val trackableFamilies = SeasonalEventCollectionBookCatalog.displayTemplates.size
        val familiesParticipated = SeasonalEventType.ALL_ORDERED.count { type ->
            cycleEntries.any { it.eventType == type }
        }
        val familiesCompleted = SeasonalEventType.ALL_ORDERED.count { type ->
            cycleEntries.any { it.eventType == type && it.isCompleted }
        }
        return SeasonalEventCollectionBookProgress(
            participatedCycles = cycleEntries.size,
            completedCycles = cycleEntries.count { it.isCompleted },
            familiesParticipated = familiesParticipated,
            familiesCompleted = familiesCompleted,
            familiesMissing = missingFamilies.size,
            trackableFamilies = trackableFamilies,
            totalCreditsEarned = cycleEntries.sumOf { it.totalCreditsEarned },
            totalXpEarned = cycleEntries.sumOf { it.totalXpEarned },
            totalFragmentsEarned = cycleEntries.sumOf { it.fragmentsEarned },
            totalAchievementsEarned = cycleEntries.sumOf { it.achievementsEarned },
        )
    }

    private fun countAchievementsEarned(
        participationAchievementEarned: Boolean,
        completionAchievementEarned: Boolean,
        masteryAchievementEarned: Boolean,
        storyFragmentEarned: Boolean,
        legendaryRelicEarned: Boolean,
    ): Int = listOf(
        participationAchievementEarned,
        completionAchievementEarned,
        masteryAchievementEarned,
        storyFragmentEarned,
        legendaryRelicEarned,
    ).count { it }
}
