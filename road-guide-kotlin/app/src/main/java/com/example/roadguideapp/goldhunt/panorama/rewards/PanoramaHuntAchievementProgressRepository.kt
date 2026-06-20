package com.example.roadguideapp.goldhunt.panorama.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.progress.AchievementProgressEngine
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PanoramaHuntAchievementProgressRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val dao = GoldHuntDatabase.get(appContext).panoramaHuntAchievementProgressDao()
    private val achievementProgressEngine by lazy { AchievementProgressEngine.get(appContext) }
    private val writeMutex = Mutex()

    suspend fun increment(
        achievementKey: String,
        increment: Int = 1,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntAchievementProgressResult {
        if (achievementKey.isBlank() || increment <= 0) {
            return PanoramaHuntAchievementProgressResult.skipped
        }
        val progress = writeMutex.withLock {
            val existing = dao.findByKey(achievementKey)
            val nextCount = (existing?.completionCount ?: 0) + increment
            val entity = PanoramaHuntAchievementProgressEntity(
                achievementKey = achievementKey,
                completionCount = nextCount,
                updatedAtMs = timestampMs,
            )
            if (existing == null) {
                dao.insert(entity)
            } else {
                dao.update(entity)
            }
            PanoramaHuntAchievementProgress(
                achievementKey = achievementKey,
                completionCount = nextCount,
                updatedAtMs = timestampMs,
            )
        }
        achievementProgressEngine.recordLegacyIncrement(
            achievementKey = achievementKey,
            increment = increment,
            timestampMs = timestampMs,
        )
        return PanoramaHuntAchievementProgressResult(
            progress = progress,
            isNewIncrement = true,
        )
    }

    suspend fun load(achievementKey: String): PanoramaHuntAchievementProgress? {
        val entity = dao.findByKey(achievementKey) ?: return null
        return PanoramaHuntAchievementProgress(
            achievementKey = entity.achievementKey,
            completionCount = entity.completionCount,
            updatedAtMs = entity.updatedAtMs,
        )
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntAchievementProgressRepository? = null

        fun get(context: Context): PanoramaHuntAchievementProgressRepository =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntAchievementProgressRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
