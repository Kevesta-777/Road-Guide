package com.example.roadguideapp.goldhunt.events.collectionbook

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCompletionDao
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventCompletionAchievementResult
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressDao
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardGrantResult
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardSchema
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressDao
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardHookDao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SeasonalEventCollectionBookRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val collectionBookDao = database.seasonalEventCollectionBookDao()
    private val progressDao: SeasonalEventProgressDao = database.seasonalEventProgressDao()
    private val completionDao: SeasonalEventAchievementCompletionDao =
        database.seasonalEventAchievementCompletionDao()
    private val achievementDao: PanoramaHuntAchievementProgressDao =
        database.panoramaHuntAchievementProgressDao()
    private val rewardHookDao: PanoramaHuntRewardHookDao = database.panoramaHuntRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun load(): SeasonalEventCollectionBook = writeMutex.withLock {
        reconcileFromProgressUnlocked()
        buildCollectionBook()
    }

    suspend fun syncFromTreasureGrant(
        event: SeasonalEvent,
        progress: SeasonalEventProgress,
        grantResult: SeasonalEventRewardGrantResult,
        isFirstJoin: Boolean,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventCollectionBookEntry? = writeMutex.withLock {
        val template = SeasonalEventCollectionBookCatalog.templateFor(event.eventType) ?: return@withLock null
        val existing = collectionBookDao.get(event.eventId)
        val firstParticipatedAtMs = when {
            isFirstJoin -> timestampMs
            existing?.firstParticipatedAtMs != null && existing.firstParticipatedAtMs > 0L ->
                existing.firstParticipatedAtMs
            else -> existing?.firstParticipatedAtMs ?: timestampMs
        }
        val entity = SeasonalEventCollectionBookMapper.toEntity(
            eventId = event.eventId,
            eventType = event.eventType,
            cycleYear = event.cycleYear,
            displayName = event.displayName,
            status = SeasonalEventCollectionBookMapper.resolveStatus(
                isCompleted = progress.isCompleted,
                treasuresCollected = progress.treasuresCollected,
            ),
            firstParticipatedAtMs = firstParticipatedAtMs,
            completedAtMs = existing?.completedAtMs,
            totalCreditsEarned = progress.creditsEarned,
            totalXpEarned = progress.xpEarned,
            fragmentsEarned = progress.fragmentsEarned,
            participationAchievementKey = template.participationAchievementKey,
            completionAchievementKey = template.completionAchievementKey,
            masteryAchievementKey = template.masteryAchievementKey,
            storyFragmentKey = template.storyFragmentKey,
            legendaryRelicKey = template.legendaryRelicKey,
            timestampMs = timestampMs,
        )
        collectionBookDao.upsert(entity)
        buildEntry(entity)
    }

    suspend fun syncFromCompletion(
        event: SeasonalEvent,
        progress: SeasonalEventProgress,
        completionResult: SeasonalEventCompletionAchievementResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventCollectionBookEntry? = writeMutex.withLock {
        if (!completionResult.isNewGrant) return@withLock null
        val template = SeasonalEventCollectionBookCatalog.templateFor(event.eventType) ?: return@withLock null
        val existing = collectionBookDao.get(event.eventId)
        val entity = SeasonalEventCollectionBookMapper.toEntity(
            eventId = event.eventId,
            eventType = event.eventType,
            cycleYear = event.cycleYear,
            displayName = event.displayName,
            status = SeasonalEventCollectionBookStatus.COMPLETED,
            firstParticipatedAtMs = existing?.firstParticipatedAtMs ?: timestampMs,
            completedAtMs = timestampMs,
            totalCreditsEarned = (existing?.totalCreditsEarned ?: 0) + completionResult.creditsGranted,
            totalXpEarned = (existing?.totalXpEarned ?: 0L) + completionResult.xpGranted,
            fragmentsEarned = progress.fragmentsEarned,
            participationAchievementKey = template.participationAchievementKey,
            completionAchievementKey = template.completionAchievementKey,
            masteryAchievementKey = template.masteryAchievementKey,
            storyFragmentKey = template.storyFragmentKey,
            legendaryRelicKey = template.legendaryRelicKey,
            timestampMs = timestampMs,
        )
        collectionBookDao.upsert(entity)
        buildEntry(entity)
    }

    private suspend fun reconcileFromProgressUnlocked() {
        val progressRows = progressDao.getAll()
        if (progressRows.isEmpty()) return
        val completionByEventId = completionDao.loadAll().associateBy { it.eventId }
        val now = System.currentTimeMillis()
        for (row in progressRows) {
            if (row.treasuresCollected <= 0) continue
            val eventType = SeasonalEventType.fromId(row.eventType) ?: continue
            val template = SeasonalEventCollectionBookCatalog.templateFor(eventType) ?: continue
            val completion = completionByEventId[row.eventId]
            val existing = collectionBookDao.get(row.eventId)
            val status = SeasonalEventCollectionBookMapper.resolveStatus(
                isCompleted = row.isCompleted,
                treasuresCollected = row.treasuresCollected,
            )
            collectionBookDao.upsert(
                SeasonalEventCollectionBookMapper.toEntity(
                    eventId = row.eventId,
                    eventType = eventType,
                    cycleYear = row.cycleYear,
                    displayName = eventType.displayName,
                    status = status,
                    firstParticipatedAtMs = existing?.firstParticipatedAtMs ?: row.updatedAtMs,
                    completedAtMs = completion?.completedAtMs ?: existing?.completedAtMs,
                    totalCreditsEarned = row.creditsEarned + (completion?.creditsGranted ?: 0),
                    totalXpEarned = row.xpEarned + (completion?.xpGranted ?: 0L),
                    fragmentsEarned = row.fragmentsEarned,
                    participationAchievementKey = template.participationAchievementKey,
                    completionAchievementKey = template.completionAchievementKey,
                    masteryAchievementKey = template.masteryAchievementKey,
                    storyFragmentKey = template.storyFragmentKey,
                    legendaryRelicKey = template.legendaryRelicKey,
                    timestampMs = now,
                ),
            )
        }
    }

    private suspend fun buildCollectionBook(): SeasonalEventCollectionBook {
        val entities = collectionBookDao.allOrdered()
        val cycleEntries = entities.mapNotNull { buildEntry(it) }
        val participatedTypes = cycleEntries.map { it.eventType }.toSet()
        val missingFamilies = SeasonalEventCollectionBookCatalog.displayTemplates
            .filter { it.eventType !in participatedTypes }
            .map { template ->
                SeasonalEventCollectionBookFamilyEntry(
                    eventType = template.eventType,
                    displayName = template.displayName,
                    minExplorerLevel = template.minExplorerLevel,
                    participationAchievementKey = template.participationAchievementKey,
                    completionAchievementKey = template.completionAchievementKey,
                    masteryAchievementKey = template.masteryAchievementKey,
                    storyFragmentKey = template.storyFragmentKey,
                    legendaryRelicKey = template.legendaryRelicKey,
                )
            }
        return SeasonalEventCollectionBook(
            cycleEntries = cycleEntries,
            missingFamilies = missingFamilies,
            progress = SeasonalEventCollectionBookMapper.buildProgress(cycleEntries, missingFamilies),
        )
    }

    private suspend fun buildEntry(
        entity: SeasonalEventCollectionBookEntryEntity,
    ): SeasonalEventCollectionBookEntry? {
        val eventType = SeasonalEventType.fromId(entity.eventType) ?: return null
        val participationEarned = isAchievementEarned(entity.participationAchievementKey)
        val completionEarned = isAchievementEarned(entity.completionAchievementKey)
        val masteryCount = achievementCount(entity.masteryAchievementKey)
        val masteryEarned = SeasonalEventCollectionBookMapper.isMasteryEarned(eventType, masteryCount)
        val storyEarned = isHookRecorded(entity.storyFragmentKey, SeasonalEventRewardSchema.HookTypes.STORY_FRAGMENT)
        val relicEarned = isHookRecorded(entity.legendaryRelicKey, SeasonalEventRewardSchema.HookTypes.LEGENDARY_RELIC)
        return SeasonalEventCollectionBookMapper.toDomain(
            entity = entity,
            participationAchievementEarned = participationEarned,
            completionAchievementEarned = completionEarned,
            masteryAchievementEarned = masteryEarned,
            storyFragmentEarned = storyEarned,
            legendaryRelicEarned = relicEarned,
        )
    }

    private suspend fun isAchievementEarned(achievementKey: String?): Boolean {
        if (achievementKey.isNullOrBlank()) return false
        return (achievementDao.findByKey(achievementKey)?.completionCount ?: 0) > 0
    }

    private suspend fun achievementCount(achievementKey: String?): Int {
        if (achievementKey.isNullOrBlank()) return 0
        return achievementDao.findByKey(achievementKey)?.completionCount ?: 0
    }

    private suspend fun isHookRecorded(hookKey: String?, hookType: String): Boolean {
        if (hookKey.isNullOrBlank()) return false
        return rewardHookDao.findHook(hookKey, hookType) != null
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventCollectionBookRepository? = null

        fun get(context: Context): SeasonalEventCollectionBookRepository =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventCollectionBookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
