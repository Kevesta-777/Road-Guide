package com.example.roadguideapp.goldhunt.achievements.badges

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AchievementBadgeRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val badgeDao: AchievementBadgeDao = database.achievementBadgeDao()
    private val hookDao: AchievementRewardHookDao = database.achievementRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun load(): AchievementBadgeCollection = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromHooksUnlocked()
        buildCollection()
    }

    suspend fun recordUnlock(
        badgeKey: String,
        achievementId: String,
        unlockedAtMs: Long,
    ): AchievementBadgeEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordUnlockUnlocked(badgeKey, achievementId, unlockedAtMs)
    }

    private suspend fun recordUnlockUnlocked(
        badgeKey: String,
        achievementId: String,
        unlockedAtMs: Long,
    ): AchievementBadgeEntry? {
        val definition = AchievementBadgeCatalog.definitionForKey(badgeKey)
            ?: AchievementBadgeCatalog.definitionForAchievement(achievementId)
            ?: return null
        val existing = badgeDao.get(definition.badgeKey)
        val unlockDate = when {
            existing?.unlockDateMs != null && existing.unlockDateMs > 0L ->
                minOf(existing.unlockDateMs, unlockedAtMs)
            else -> unlockedAtMs
        }
        val entity = AchievementBadgeMapper.toEntity(
            definition = definition,
            unlockDateMs = unlockDate,
            timestampMs = unlockedAtMs,
        )
        badgeDao.upsert(entity)
        return AchievementBadgeMapper.toDomain(definition, entity)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = badgeDao.all().map { it.badgeKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = AchievementBadgeCatalog.definitions
            .filter { it.badgeKey !in existingKeys }
            .map { definition ->
                AchievementBadgeMapper.toEntity(
                    definition = definition,
                    unlockDateMs = null,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            badgeDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromHooksUnlocked() {
        val hooks = hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.BADGE)
        if (hooks.isEmpty()) return
        for (hook in hooks) {
            recordUnlockUnlocked(
                badgeKey = hook.hookKey,
                achievementId = hook.achievementId,
                unlockedAtMs = hook.grantedAtMs,
            )
        }
    }

    private suspend fun buildCollection(): AchievementBadgeCollection {
        val entities = badgeDao.all().associateBy { it.badgeKey }
        val entries = AchievementBadgeCatalog.definitions.map { definition ->
            AchievementBadgeMapper.toDomain(definition, entities[definition.badgeKey])
        }
        return AchievementBadgeCollection(
            entries = entries,
            progress = AchievementBadgeMapper.buildProgress(entries),
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementBadgeRepository? = null

        fun get(context: Context): AchievementBadgeRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementBadgeRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
