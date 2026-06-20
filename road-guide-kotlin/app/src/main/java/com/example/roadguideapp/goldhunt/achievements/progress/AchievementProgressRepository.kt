package com.example.roadguideapp.goldhunt.achievements.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCatalog
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDao
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementMapper
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainContext
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainResolver
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AchievementProgressRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val achievementDao: AchievementDao = GoldHuntDatabase.get(appContext).achievementDao()
    private val writeMutex = Mutex()

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()): Unit = writeMutex.withLock {
        ensureSeededUnlocked(timestampMs)
    }

    suspend fun load(achievementId: String): Achievement? = writeMutex.withLock {
        achievementDao.get(achievementId)?.let { AchievementMapper.toDomain(it) }
    }

    suspend fun loadAll(): List<Achievement> = writeMutex.withLock {
        achievementDao.getAll().mapNotNull { AchievementMapper.toDomain(it) }
    }

    suspend fun loadByCategory(category: AchievementCategory): List<Achievement> = writeMutex.withLock {
        achievementDao.getByCategory(category.id).mapNotNull { AchievementMapper.toDomain(it) }
    }

    suspend fun applyValue(
        achievementId: String,
        proposedValue: Int,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementProgressResult = writeMutex.withLock {
        ensureSeededUnlocked(timestampMs)
        val existing = getOrSeedUnlocked(achievementId, timestampMs)
            ?: return@withLock AchievementProgressResult.skipped
        if (!isChainProgressAllowedUnlocked(achievementId)) {
            return@withLock AchievementProgressResult.skipped
        }
        val result = AchievementProgressUpdater.applyValue(existing, proposedValue, timestampMs)
        if (result.progressChanged && result.achievement != null) {
            achievementDao.upsert(AchievementMapper.toEntity(result.achievement, timestampMs))
        }
        result
    }

    suspend fun applyIncrement(
        achievementId: String,
        increment: Int,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementProgressResult = writeMutex.withLock {
        ensureSeededUnlocked(timestampMs)
        val existing = getOrSeedUnlocked(achievementId, timestampMs)
            ?: return@withLock AchievementProgressResult.skipped
        if (!isChainProgressAllowedUnlocked(achievementId)) {
            return@withLock AchievementProgressResult.skipped
        }
        val result = AchievementProgressUpdater.applyIncrement(existing, increment, timestampMs)
        if (result.progressChanged && result.achievement != null) {
            achievementDao.upsert(AchievementMapper.toEntity(result.achievement, timestampMs))
        }
        result
    }

    suspend fun reconcileDefinitions(
        definitions: List<AchievementDefinition>,
        counters: AchievementProgressCounters,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementProgressBatchResult = writeMutex.withLock {
        ensureSeededUnlocked(timestampMs)
        val chainContext = buildChainContextUnlocked()
        val results = definitions.map { definition ->
            if (!AchievementChainResolver.isProgressAllowed(definition.key, chainContext)) {
                return@map AchievementProgressResult.skipped
            }
            val proposed = AchievementProgressEvaluator.counterValue(definition, counters)
            val existing = achievementDao.get(definition.key)?.let { AchievementMapper.toDomain(it) }
                ?: AchievementCatalog.achievementFromDefinition(definition)
            val result = AchievementProgressUpdater.applyValue(existing, proposed, timestampMs)
            if (result.progressChanged && result.achievement != null) {
                achievementDao.upsert(AchievementMapper.toEntity(result.achievement, timestampMs))
            }
            result
        }
        AchievementProgressBatchResult(results)
    }

    private suspend fun ensureSeededUnlocked(timestampMs: Long) {
        val existingIds = achievementDao.getAll().map { it.achievementId }.toSet()
        val missing = AchievementRegistry.allDefinitions().filter { it.key !in existingIds }
        if (missing.isEmpty()) return
        achievementDao.upsertAll(
            missing.map { definition ->
                AchievementMapper.toEntity(
                    AchievementCatalog.achievementFromDefinition(definition),
                    updatedAtMs = timestampMs,
                )
            },
        )
    }

    private suspend fun isChainProgressAllowedUnlocked(achievementId: String): Boolean {
        val context = buildChainContextUnlocked()
        return AchievementChainResolver.isProgressAllowed(achievementId, context)
    }

    private suspend fun buildChainContextUnlocked(): AchievementChainContext {
        val achievements = achievementDao.getAll().mapNotNull { AchievementMapper.toDomain(it) }
        val achievementsByKey = achievements.associateBy { it.achievementId }
        val chainHooks = GoldHuntDatabase.get(appContext).achievementRewardHookDao()
            .getAllByHookType(AchievementRewardSchema.HookTypes.CHAIN_UNLOCK)
        return AchievementChainResolver.buildContext(
            achievementsByKey = achievementsByKey,
            chainHooks = chainHooks,
        )
    }

    private suspend fun getOrSeedUnlocked(achievementId: String, timestampMs: Long): Achievement? {
        achievementDao.get(achievementId)?.let { return AchievementMapper.toDomain(it) }
        val definition = AchievementRegistry.findByKey(achievementId) ?: return null
        val achievement = AchievementCatalog.achievementFromDefinition(definition)
        achievementDao.upsert(AchievementMapper.toEntity(achievement, timestampMs))
        return achievement
    }

    companion object {
        @Volatile
        private var instance: AchievementProgressRepository? = null

        fun get(context: Context): AchievementProgressRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementProgressRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
