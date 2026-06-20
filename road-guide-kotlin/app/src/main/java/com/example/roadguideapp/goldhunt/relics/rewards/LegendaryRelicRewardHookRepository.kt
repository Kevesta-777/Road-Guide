package com.example.roadguideapp.goldhunt.relics.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class LegendaryRelicRewardHookRecordResult(
    val storyFragmentRecorded: Boolean = false,
    val titleRecorded: Boolean = false,
    val badgeRecorded: Boolean = false,
    val cosmeticRecorded: Boolean = false,
)

internal class LegendaryRelicRewardHookRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).legendaryRelicRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun recordCompletionHooks(
        outcome: LegendaryRelicRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicRewardHookRecordResult = writeMutex.withLock {
        val relicId = outcome.relic.relicId
        LegendaryRelicRewardHookRecordResult(
            storyFragmentRecorded = recordHook(
                eventId = outcome.storyFragmentEventId(),
                relicId = relicId,
                hookType = LegendaryRelicRewardSchema.HookTypes.STORY_FRAGMENT,
                hookKey = outcome.storyFragmentKey,
                grantedAtMs = timestampMs,
            ),
            titleRecorded = recordHook(
                eventId = outcome.titleEventId(),
                relicId = relicId,
                hookType = LegendaryRelicRewardSchema.HookTypes.TITLE,
                hookKey = outcome.titleKey,
                grantedAtMs = timestampMs,
            ),
            badgeRecorded = recordHook(
                eventId = outcome.badgeEventId(),
                relicId = relicId,
                hookType = LegendaryRelicRewardSchema.HookTypes.BADGE,
                hookKey = outcome.badgeKey,
                grantedAtMs = timestampMs,
            ),
            cosmeticRecorded = recordHook(
                eventId = outcome.cosmeticEventId(),
                relicId = relicId,
                hookType = LegendaryRelicRewardSchema.HookTypes.COSMETIC,
                hookKey = outcome.cosmeticKey,
                grantedAtMs = timestampMs,
            ),
        )
    }

    suspend fun recordChainHook(
        hook: LegendaryRelicRewardHookEntity,
    ): Boolean = writeMutex.withLock {
        recordHook(
            eventId = hook.eventId,
            relicId = hook.relicId,
            hookType = hook.hookType,
            hookKey = hook.hookKey,
            grantedAtMs = hook.grantedAtMs,
        )
    }

    suspend fun loadChainUnlockHooks(): List<LegendaryRelicRewardHookEntity> =
        dao.getAllByHookType(LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK)

    private suspend fun recordHook(
        eventId: String?,
        relicId: String,
        hookType: String,
        hookKey: String?,
        grantedAtMs: Long,
    ): Boolean {
        if (eventId.isNullOrBlank() || hookKey.isNullOrBlank()) return false
        val inserted = dao.insert(
            LegendaryRelicRewardHookEntity(
                eventId = eventId,
                relicId = relicId,
                hookType = hookType,
                hookKey = hookKey,
                grantedAtMs = grantedAtMs,
            ),
        )
        return inserted >= 0L
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicRewardHookRepository? = null

        fun get(context: Context): LegendaryRelicRewardHookRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicRewardHookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
