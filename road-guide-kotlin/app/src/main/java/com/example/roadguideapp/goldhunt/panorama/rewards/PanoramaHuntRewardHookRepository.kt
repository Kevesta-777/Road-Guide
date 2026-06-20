package com.example.roadguideapp.goldhunt.panorama.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class PanoramaHuntRewardHookRecordResult(
    val storyFragmentRecorded: Boolean = false,
    val achievementRecorded: Boolean = false,
    val legendaryRelicRecorded: Boolean = false,
)

internal class PanoramaHuntRewardHookRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).panoramaHuntRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun recordCompletionHooks(
        outcome: PanoramaHuntRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntRewardHookRecordResult = writeMutex.withLock {
        PanoramaHuntRewardHookRecordResult(
            storyFragmentRecorded = recordHook(
                eventId = outcome.storyFragmentEventId(),
                huntId = outcome.hunt.huntId,
                hookType = PanoramaHuntRewardSchema.HookTypes.STORY_FRAGMENT,
                hookKey = outcome.bundle.storyFragmentId,
                grantedAtMs = timestampMs,
            ),
            achievementRecorded = recordHook(
                eventId = outcome.achievementEventId(),
                huntId = outcome.hunt.huntId,
                hookType = PanoramaHuntRewardSchema.HookTypes.ACHIEVEMENT,
                hookKey = outcome.bundle.achievementKey,
                grantedAtMs = timestampMs,
            ),
            legendaryRelicRecorded = recordHook(
                eventId = outcome.legendaryRelicEventId(),
                huntId = outcome.hunt.huntId,
                hookType = PanoramaHuntRewardSchema.HookTypes.LEGENDARY_RELIC,
                hookKey = outcome.bundle.legendaryRelicKey,
                grantedAtMs = timestampMs,
            ),
        )
    }

    private suspend fun recordHook(
        eventId: String?,
        huntId: String,
        hookType: String,
        hookKey: String?,
        grantedAtMs: Long,
    ): Boolean {
        if (eventId.isNullOrBlank() || hookKey.isNullOrBlank()) return false
        val inserted = dao.insert(
            PanoramaHuntRewardHookEntity(
                eventId = eventId,
                huntId = huntId,
                hookType = hookType,
                hookKey = hookKey,
                grantedAtMs = grantedAtMs,
            ),
        )
        return inserted >= 0L
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntRewardHookRepository? = null

        fun get(context: Context): PanoramaHuntRewardHookRepository =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntRewardHookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
