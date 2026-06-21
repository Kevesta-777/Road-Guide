package com.example.roadguideapp.goldhunt.panorama.completion

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalRepository
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntStatisticsManager
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class PanoramaHuntCompletionRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val findingDao = GoldHuntDatabase.get(appContext).panoramaHuntTargetFindingDao()
    private val completionDao = GoldHuntDatabase.get(appContext).panoramaHuntCompletionDao()
    private val statisticsManager = PanoramaHuntStatisticsManager.get(appContext)
    private val journalRepository = PanoramaHuntJournalRepository.get(appContext)
    private val writeMutex = Mutex()
    private val completionCache = ConcurrentHashMap<String, Boolean>()
    private val findingsCache = ConcurrentHashMap<String, MutableSet<Int>>()
    private var cacheLoaded = false

    suspend fun ensureCacheLoaded() {
        if (cacheLoaded) return
        writeMutex.withLock {
            if (cacheLoaded) return@withLock
            for (row in completionDao.allOrdered()) {
                completionCache[row.huntId] = true
            }
            cacheLoaded = true
        }
    }

    suspend fun loadProgress(
        huntId: String,
        targetSet: PanoramaHiddenTargetSet,
    ): PanoramaHuntProgress = writeMutex.withLock {
        ensureCacheLoadedUnlocked()
        val slots = loadCollectedSlotsUnlocked(huntId)
        val completion = completionDao.findByHuntId(huntId)
        PanoramaHuntCompletionMapper.toProgress(
            huntId = huntId,
            collectedSlotIndices = slots,
            targetsTotal = targetSet.targets.size,
            completedAtMs = completion?.completedAtMs,
        )
    }

    fun isHuntCompletedCached(huntId: String): Boolean = completionCache[huntId] == true

    suspend fun recordTargetFinding(
        target: PanoramaHiddenTarget,
        foundAtMs: Long,
    ): PanoramaHuntTargetFindingRecordResult = writeMutex.withLock {
        ensureCacheLoadedUnlocked()
        val slots = loadCollectedSlotsUnlocked(target.huntId)
        if (target.slotIndex in slots) {
            return@withLock PanoramaHuntTargetFindingRecordResult(
                isNew = false,
                slotIndex = target.slotIndex,
            )
        }
        val inserted = findingDao.insert(
            PanoramaHuntCompletionMapper.toTargetFindingEntity(target, foundAtMs),
        )
        if (inserted < 0L) {
            slots += target.slotIndex
            findingsCache[target.huntId] = slots
            return@withLock PanoramaHuntTargetFindingRecordResult(
                isNew = false,
                slotIndex = target.slotIndex,
            )
        }
        slots += target.slotIndex
        findingsCache[target.huntId] = slots
        statisticsManager.recordTargetFound(target.targetType, foundAtMs)
        PanoramaHuntTargetFindingRecordResult(
            isNew = true,
            slotIndex = target.slotIndex,
        )
    }

    suspend fun recordHuntCompletion(
        hunt: PanoramaHunt,
        targetSet: PanoramaHiddenTargetSet,
        creditsGranted: Int,
        xpGranted: Long,
        completedAtMs: Long,
    ): PanoramaHuntCompletionRecordResult = writeMutex.withLock {
        ensureCacheLoadedUnlocked()
        if (isHuntCompletedCached(hunt.huntId)) {
            return@withLock PanoramaHuntCompletionRecordResult.skipped
        }
        val slots = loadCollectedSlotsUnlocked(hunt.huntId)
        val entity = PanoramaHuntCompletionMapper.toCompletionEntity(
            hunt = hunt,
            targetsFound = slots.size.coerceAtMost(targetSet.targets.size),
            targetsTotal = targetSet.targets.size,
            creditsGranted = creditsGranted,
            xpGranted = xpGranted,
            completedAtMs = completedAtMs,
        )
        val inserted = completionDao.insert(entity)
        if (inserted < 0L) {
            completionCache[hunt.huntId] = true
            return@withLock PanoramaHuntCompletionRecordResult.skipped
        }
        completionCache[hunt.huntId] = true
        statisticsManager.recordHuntCompletion(
            creditsEarned = creditsGranted,
            xpEarned = xpGranted,
            timestampMs = completedAtMs,
        )
        journalRepository.recordCompletion(
            hunt = hunt,
            creditsEarned = creditsGranted,
            xpEarned = xpGranted,
            completedAtMs = completedAtMs,
        )
        val completion = PanoramaHuntCompletionMapper.toDomain(entity)
            ?: return@withLock PanoramaHuntCompletionRecordResult.skipped
        PanoramaHuntCompletionRecordResult(
            completion = completion,
            isNew = true,
            creditsGranted = creditsGranted,
            xpGranted = xpGranted,
        )
    }

    private suspend fun ensureCacheLoadedUnlocked() {
        if (cacheLoaded) return
        for (row in completionDao.allOrdered()) {
            completionCache[row.huntId] = true
        }
        cacheLoaded = true
    }

    private suspend fun loadCollectedSlotsUnlocked(huntId: String): MutableSet<Int> {
        findingsCache[huntId]?.let { return it.toMutableSet() }
        val slots = findingDao.slotIndicesForHunt(huntId).toMutableSet()
        findingsCache[huntId] = slots
        return slots
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntCompletionRepository? = null

        fun get(context: Context): PanoramaHuntCompletionRepository =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntCompletionRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
