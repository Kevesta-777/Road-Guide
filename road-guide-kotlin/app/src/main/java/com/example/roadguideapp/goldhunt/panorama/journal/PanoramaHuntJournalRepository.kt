package com.example.roadguideapp.goldhunt.panorama.journal

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardSchema
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PanoramaHuntJournalRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val journalDao = database.panoramaHuntJournalDao()
    private val sessionDao = database.panoramaHuntSessionDao()
    private val completionDao = database.panoramaHuntCompletionDao()
    private val achievementDao = database.panoramaHuntAchievementProgressDao()
    private val rewardHookDao = database.panoramaHuntRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun load(): PanoramaHuntJournal = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromPanoramaTablesUnlocked()
        buildJournal()
    }

    suspend fun recordDiscovery(
        hunt: PanoramaHunt,
        discoveredAtMs: Long,
    ): PanoramaHuntJournalEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordDiscoveryUnlocked(hunt.huntType, discoveredAtMs)
    }

    suspend fun recordCompletion(
        hunt: PanoramaHunt,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): PanoramaHuntJournalEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordCompletionUnlocked(hunt, creditsEarned, xpEarned, completedAtMs)
    }

    private suspend fun recordDiscoveryUnlocked(
        huntType: PanoramaHuntType,
        discoveredAtMs: Long,
    ): PanoramaHuntJournalEntry? {
        val template = PanoramaHuntJournalCatalog.templateForType(huntType) ?: return null
        val existing = journalDao.get(huntType.id)
        val discoveredCount = (existing?.discoveredCount ?: 0) + 1
        val completedCount = existing?.completedCount ?: 0
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: discoveredAtMs
        val entity = PanoramaHuntJournalMapper.toEntity(
            huntType = huntType,
            status = PanoramaHuntJournalMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = existing?.completedAtMs,
            totalRewardsEarned = existing?.totalRewardsEarned ?: 0,
            totalXpEarned = existing?.totalXpEarned ?: 0L,
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            achievementKey = existing?.achievementKey ?: template.achievementKey,
            storyFragmentId = existing?.storyFragmentId ?: template.storyFragmentKey,
            legendaryRelicKey = existing?.legendaryRelicKey ?: template.legendaryRelicKey,
            timestampMs = discoveredAtMs,
        )
        journalDao.upsert(entity)
        return buildEntry(template, entity)
    }

    private suspend fun recordCompletionUnlocked(
        hunt: PanoramaHunt,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): PanoramaHuntJournalEntry? {
        val template = PanoramaHuntJournalCatalog.templateForType(hunt.huntType) ?: return null
        val existing = journalDao.get(hunt.huntType.id)
        val discoveredCount = maxOf(existing?.discoveredCount ?: 0, 1)
        val completedCount = (existing?.completedCount ?: 0) + 1
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: completedAtMs
        val completedAt = maxOf(existing?.completedAtMs ?: 0L, completedAtMs).takeIf { it > 0L }
        val achievementKey = hunt.resolvedAchievementKey ?: template.achievementKey
        val storyFragmentId = hunt.resolvedStoryFragmentId ?: template.storyFragmentKey
        val legendaryRelicKey = hunt.resolvedLegendaryRelicKey ?: template.legendaryRelicKey
        val entity = PanoramaHuntJournalMapper.toEntity(
            huntType = hunt.huntType,
            status = PanoramaHuntJournalMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = completedAt,
            totalRewardsEarned = (existing?.totalRewardsEarned ?: 0) + creditsEarned.coerceAtLeast(0),
            totalXpEarned = (existing?.totalXpEarned ?: 0L) + xpEarned.coerceAtLeast(0L),
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            achievementKey = achievementKey,
            storyFragmentId = storyFragmentId,
            legendaryRelicKey = legendaryRelicKey,
            timestampMs = completedAtMs,
        )
        journalDao.upsert(entity)
        return buildEntry(template, entity)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = journalDao.all().map { it.catalogKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = PanoramaHuntJournalCatalog.displayTemplates
            .filter { it.huntType.id !in existingKeys }
            .map { template ->
                PanoramaHuntJournalMapper.toEntity(
                    huntType = template.huntType,
                    status = PanoramaHuntJournalCatalog.initialStatus(),
                    firstDiscoveredAtMs = null,
                    completedAtMs = null,
                    totalRewardsEarned = 0,
                    totalXpEarned = 0L,
                    discoveredCount = 0,
                    completedCount = 0,
                    achievementKey = template.achievementKey,
                    storyFragmentId = template.storyFragmentKey,
                    legendaryRelicKey = template.legendaryRelicKey,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            journalDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromPanoramaTablesUnlocked() {
        val sessions = sessionDao.allOrdered()
        val completions = completionDao.allOrdered()
        if (sessions.isEmpty() && completions.isEmpty()) return
        val now = System.currentTimeMillis()
        val sessionsByType = sessions.groupBy { it.huntType }
        val completionsByType = completions.groupBy { it.huntType }
        for (template in PanoramaHuntJournalCatalog.displayTemplates) {
            val typeId = template.huntType.id
            val typeSessions = sessionsByType[typeId].orEmpty()
            val typeCompletions = completionsByType[typeId].orEmpty()
            if (typeSessions.isEmpty() && typeCompletions.isEmpty()) continue
            val discoveredCount = typeSessions.size
            val completedCount = typeCompletions.size
            val firstDiscoveredAtMs = typeSessions.minOfOrNull { it.startedAtMs }
            val completedAtMs = typeCompletions.maxOfOrNull { it.completedAtMs }
            val totalRewards = typeCompletions.sumOf { it.creditsGranted.coerceAtLeast(0) }
            val totalXp = typeCompletions.sumOf { it.xpGranted.coerceAtLeast(0L) }
            val latestCompletion = typeCompletions.maxByOrNull { it.completedAtMs }
            journalDao.upsert(
                PanoramaHuntJournalMapper.toEntity(
                    huntType = template.huntType,
                    status = PanoramaHuntJournalMapper.resolveStatus(discoveredCount, completedCount),
                    firstDiscoveredAtMs = firstDiscoveredAtMs,
                    completedAtMs = completedAtMs,
                    totalRewardsEarned = totalRewards,
                    totalXpEarned = totalXp,
                    discoveredCount = discoveredCount,
                    completedCount = completedCount,
                    achievementKey = latestCompletion?.achievementKey ?: template.achievementKey,
                    storyFragmentId = latestCompletion?.storyFragmentId ?: template.storyFragmentKey,
                    legendaryRelicKey = latestCompletion?.legendaryRelicKey ?: template.legendaryRelicKey,
                    timestampMs = now,
                ),
            )
        }
    }

    private suspend fun buildJournal(): PanoramaHuntJournal {
        val entities = journalDao.all().associateBy { it.catalogKey }
        val entries = PanoramaHuntJournalCatalog.displayTemplates.map { template ->
            buildEntry(template, entities[template.huntType.id])
        }
        return PanoramaHuntJournal(
            entries = entries,
            progress = PanoramaHuntJournalMapper.buildProgress(entries),
        )
    }

    private suspend fun buildEntry(
        template: PanoramaHuntJournalTemplate,
        entity: PanoramaHuntJournalEntryEntity?,
    ): PanoramaHuntJournalEntry {
        val achievementKey = entity?.achievementKey ?: template.achievementKey
        val storyKey = entity?.storyFragmentId ?: template.storyFragmentKey
        val relicKey = entity?.legendaryRelicKey ?: template.legendaryRelicKey
        return PanoramaHuntJournalMapper.toDomain(
            template = template,
            entity = entity,
            achievementUnlocked = isAchievementUnlocked(achievementKey),
            storyFragmentUnlocked = isHookRecorded(
                hookKey = storyKey,
                hookType = PanoramaHuntRewardSchema.HookTypes.STORY_FRAGMENT,
            ),
            legendaryRelicUnlocked = isHookRecorded(
                hookKey = relicKey,
                hookType = PanoramaHuntRewardSchema.HookTypes.LEGENDARY_RELIC,
            ),
        )
    }

    private suspend fun isAchievementUnlocked(achievementKey: String?): Boolean {
        if (achievementKey.isNullOrBlank()) return false
        return (achievementDao.findByKey(achievementKey)?.completionCount ?: 0) > 0
    }

    private suspend fun isHookRecorded(hookKey: String?, hookType: String): Boolean {
        if (hookKey.isNullOrBlank()) return false
        return rewardHookDao.findHook(hookKey, hookType) != null
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntJournalRepository? = null

        fun get(context: Context): PanoramaHuntJournalRepository =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntJournalRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
