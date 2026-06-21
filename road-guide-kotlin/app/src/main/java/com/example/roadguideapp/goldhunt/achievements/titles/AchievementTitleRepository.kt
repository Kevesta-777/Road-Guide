package com.example.roadguideapp.goldhunt.achievements.titles

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileDao
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AchievementTitleRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val titleDao: AchievementTitleDao = database.achievementTitleDao()
    private val hookDao: AchievementRewardHookDao = database.achievementRewardHookDao()
    private val explorerProfileDao: ExplorerProfileDao = database.explorerProfileDao()
    private val writeMutex = Mutex()

    suspend fun load(): AchievementTitleCollection = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromHooksUnlocked()
        buildCollection()
    }

    suspend fun recordUnlock(
        titleKey: String,
        achievementId: String,
        unlockedAtMs: Long,
    ): AchievementTitleEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordUnlockUnlocked(titleKey, achievementId, unlockedAtMs)
    }

    suspend fun setActiveTitle(titleKey: String): AchievementTitleSelectionResult =
        writeMutex.withLock {
            ensureSeededUnlocked()
            reconcileFromHooksUnlocked()
            val definition = AchievementTitleCatalog.definitionForKey(titleKey)
                ?: return@withLock AchievementTitleSelectionResult.UnknownTitle
            val entity = titleDao.get(definition.titleKey)
            if (entity?.unlockDateMs == null || entity.unlockDateMs <= 0L) {
                return@withLock AchievementTitleSelectionResult.NotUnlocked
            }
            val timestampMs = System.currentTimeMillis()
            explorerProfileDao.updateActiveAchievementTitleKey(
                titleKey = definition.titleKey,
                timestampMs = timestampMs,
            )
            val entry = AchievementTitleMapper.toDomain(
                definition = definition,
                entity = entity,
                activeTitleKey = definition.titleKey,
            )
            AchievementTitleSelectionResult.Active(entry)
        }

    suspend fun clearActiveTitle(): AchievementTitleSelectionResult = writeMutex.withLock {
        explorerProfileDao.updateActiveAchievementTitleKey(
            titleKey = null,
            timestampMs = System.currentTimeMillis(),
        )
        AchievementTitleSelectionResult.Cleared
    }

    private suspend fun recordUnlockUnlocked(
        titleKey: String,
        achievementId: String,
        unlockedAtMs: Long,
    ): AchievementTitleEntry? {
        val definition = AchievementTitleCatalog.definitionForKey(titleKey)
            ?: AchievementTitleCatalog.definitionForAchievement(achievementId)
            ?: return null
        val existing = titleDao.get(definition.titleKey)
        val unlockDate = when {
            existing?.unlockDateMs != null && existing.unlockDateMs > 0L ->
                minOf(existing.unlockDateMs, unlockedAtMs)
            else -> unlockedAtMs
        }
        val entity = AchievementTitleMapper.toEntity(
            definition = definition,
            unlockDateMs = unlockDate,
            timestampMs = unlockedAtMs,
        )
        titleDao.upsert(entity)
        val activeTitleKey = explorerProfileDao.get()?.activeAchievementTitleKey
        return AchievementTitleMapper.toDomain(definition, entity, activeTitleKey)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = titleDao.all().map { it.titleKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = AchievementTitleCatalog.definitions
            .filter { it.titleKey !in existingKeys }
            .map { definition ->
                AchievementTitleMapper.toEntity(
                    definition = definition,
                    unlockDateMs = null,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            titleDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromHooksUnlocked() {
        val hooks = hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.TITLE)
        if (hooks.isEmpty()) return
        for (hook in hooks) {
            recordUnlockUnlocked(
                titleKey = hook.hookKey,
                achievementId = hook.achievementId,
                unlockedAtMs = hook.grantedAtMs,
            )
        }
        reconcileActiveTitleUnlocked()
    }

    private suspend fun reconcileActiveTitleUnlocked() {
        val profile = explorerProfileDao.get() ?: return
        val activeTitleKey = profile.activeAchievementTitleKey ?: return
        val entity = titleDao.get(activeTitleKey)
        if (entity?.unlockDateMs != null && entity.unlockDateMs > 0L) return
        explorerProfileDao.updateActiveAchievementTitleKey(
            titleKey = null,
            timestampMs = System.currentTimeMillis(),
        )
    }

    private suspend fun buildCollection(): AchievementTitleCollection {
        val activeTitleKey = explorerProfileDao.get()?.activeAchievementTitleKey
        val entities = titleDao.all().associateBy { it.titleKey }
        val entries = AchievementTitleCatalog.definitions.map { definition ->
            AchievementTitleMapper.toDomain(
                definition = definition,
                entity = entities[definition.titleKey],
                activeTitleKey = activeTitleKey,
            )
        }
        return AchievementTitleCollection(
            entries = entries,
            progress = AchievementTitleMapper.buildProgress(entries),
            activeTitleKey = activeTitleKey,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementTitleRepository? = null

        fun get(context: Context): AchievementTitleRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementTitleRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
