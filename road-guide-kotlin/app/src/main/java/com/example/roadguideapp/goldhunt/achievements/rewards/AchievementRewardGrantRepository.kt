package com.example.roadguideapp.goldhunt.achievements.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class AchievementRewardGrantRecord(
    val entity: AchievementRewardGrantEntity,
    val isNew: Boolean,
)

internal class AchievementRewardGrantRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).achievementRewardGrantDao()
    private val writeMutex = Mutex()

    suspend fun isGranted(achievementId: String): Boolean =
        dao.findAchievementId(achievementId) != null

    suspend fun recordGrant(
        entity: AchievementRewardGrantEntity,
    ): AchievementRewardGrantRecord = writeMutex.withLock {
        if (dao.findAchievementId(entity.achievementId) != null) {
            return@withLock AchievementRewardGrantRecord(entity = entity, isNew = false)
        }
        val inserted = dao.insert(entity)
        AchievementRewardGrantRecord(
            entity = entity,
            isNew = inserted >= 0L,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementRewardGrantRepository? = null

        fun get(context: Context): AchievementRewardGrantRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementRewardGrantRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
