package com.example.roadguideapp.goldhunt.events.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardHookEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class SeasonalEventRewardHookRecordResult(
    val participationRecorded: Boolean = false,
    val storyFragmentRecorded: Boolean = false,
    val achievementRecorded: Boolean = false,
    val legendaryRelicRecorded: Boolean = false,
)

internal class SeasonalEventRewardHookRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).panoramaHuntRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun recordCompletionHook(
        event: SeasonalEvent,
        achievementKey: String,
        timestampMs: Long = System.currentTimeMillis(),
    ): Boolean = writeMutex.withLock {
        recordHook(
            eventId = "${SeasonalEventRewardSchema.EventPrefixes.ACHIEVEMENT}:completion:${event.eventId}",
            sourceId = event.eventId,
            hookType = SeasonalEventRewardSchema.HookTypes.ACHIEVEMENT,
            hookKey = achievementKey,
            grantedAtMs = timestampMs,
        )
    }

    suspend fun recordTreasureHooks(
        outcome: SeasonalEventRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventRewardHookRecordResult = writeMutex.withLock {
        SeasonalEventRewardHookRecordResult(
            participationRecorded = recordHook(
                eventId = "${SeasonalEventRewardSchema.EventPrefixes.PARTICIPATION}:${outcome.participationEventId()}",
                sourceId = outcome.event.eventId,
                hookType = SeasonalEventRewardSchema.HookTypes.ACHIEVEMENT,
                hookKey = outcome.bundle.achievementKey,
                grantedAtMs = timestampMs,
            ),
            storyFragmentRecorded = recordHook(
                eventId = outcome.storyFragmentEventId(),
                sourceId = outcome.event.eventId,
                hookType = SeasonalEventRewardSchema.HookTypes.STORY_FRAGMENT,
                hookKey = outcome.bundle.storyFragmentKey,
                grantedAtMs = timestampMs,
            ),
            achievementRecorded = recordHook(
                eventId = outcome.achievementEventId(),
                sourceId = outcome.event.eventId,
                hookType = SeasonalEventRewardSchema.HookTypes.ACHIEVEMENT,
                hookKey = outcome.bundle.achievementKey,
                grantedAtMs = timestampMs,
            ),
            legendaryRelicRecorded = recordHook(
                eventId = outcome.legendaryRelicEventId(),
                sourceId = outcome.event.eventId,
                hookType = SeasonalEventRewardSchema.HookTypes.LEGENDARY_RELIC,
                hookKey = outcome.bundle.legendaryRelicKey,
                grantedAtMs = timestampMs,
            ),
        )
    }

    private suspend fun recordHook(
        eventId: String?,
        sourceId: String,
        hookType: String,
        hookKey: String?,
        grantedAtMs: Long,
    ): Boolean {
        if (eventId.isNullOrBlank() || hookKey.isNullOrBlank()) return false
        val inserted = dao.insert(
            PanoramaHuntRewardHookEntity(
                eventId = eventId,
                huntId = sourceId,
                hookType = hookType,
                hookKey = hookKey,
                grantedAtMs = grantedAtMs,
            ),
        )
        return inserted >= 0L
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventRewardHookRepository? = null

        fun get(context: Context): SeasonalEventRewardHookRepository =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventRewardHookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
