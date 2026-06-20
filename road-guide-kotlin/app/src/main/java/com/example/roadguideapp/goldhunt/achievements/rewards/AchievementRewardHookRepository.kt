package com.example.roadguideapp.goldhunt.achievements.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class AchievementRewardHookRecordResult(
    val storyFragmentRecorded: Boolean = false,
    val legendaryRelicRecorded: Boolean = false,
    val titleRecorded: Boolean = false,
    val badgeRecorded: Boolean = false,
)

internal class AchievementRewardHookRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).achievementRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun recordCompletionHooks(
        outcome: AchievementRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementRewardHookRecordResult = writeMutex.withLock {
        val achievementId = outcome.achievement.achievementId
        AchievementRewardHookRecordResult(
            storyFragmentRecorded = recordHook(
                eventId = outcome.storyFragmentEventId(),
                achievementId = achievementId,
                hookType = AchievementRewardSchema.HookTypes.STORY_FRAGMENT,
                hookKey = outcome.storyFragmentKey,
                grantedAtMs = timestampMs,
            ),
            legendaryRelicRecorded = recordHook(
                eventId = outcome.legendaryRelicEventId(),
                achievementId = achievementId,
                hookType = AchievementRewardSchema.HookTypes.LEGENDARY_RELIC,
                hookKey = outcome.legendaryRelicKey,
                grantedAtMs = timestampMs,
            ),
            titleRecorded = recordHook(
                eventId = outcome.titleEventId(),
                achievementId = achievementId,
                hookType = AchievementRewardSchema.HookTypes.TITLE,
                hookKey = outcome.titleKey,
                grantedAtMs = timestampMs,
            ),
            badgeRecorded = recordHook(
                eventId = outcome.badgeEventId(),
                achievementId = achievementId,
                hookType = AchievementRewardSchema.HookTypes.BADGE,
                hookKey = outcome.badgeKey,
                grantedAtMs = timestampMs,
            ),
        )
    }

    suspend fun recordChainHook(
        hook: AchievementRewardHookEntity,
    ): Boolean = writeMutex.withLock {
        recordHook(
            eventId = hook.eventId,
            achievementId = hook.achievementId,
            hookType = hook.hookType,
            hookKey = hook.hookKey,
            grantedAtMs = hook.grantedAtMs,
        )
    }

    suspend fun loadChainUnlockHooks(): List<AchievementRewardHookEntity> =
        dao.getAllByHookType(AchievementRewardSchema.HookTypes.CHAIN_UNLOCK)

    private suspend fun recordHook(
        eventId: String?,
        achievementId: String,
        hookType: String,
        hookKey: String?,
        grantedAtMs: Long,
    ): Boolean {
        if (eventId.isNullOrBlank() || hookKey.isNullOrBlank()) return false
        val inserted = dao.insert(
            AchievementRewardHookEntity(
                eventId = eventId,
                achievementId = achievementId,
                hookType = hookType,
                hookKey = hookKey,
                grantedAtMs = grantedAtMs,
            ),
        )
        return inserted >= 0L
    }

    companion object {
        @Volatile
        private var instance: AchievementRewardHookRepository? = null

        fun get(context: Context): AchievementRewardHookRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementRewardHookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
