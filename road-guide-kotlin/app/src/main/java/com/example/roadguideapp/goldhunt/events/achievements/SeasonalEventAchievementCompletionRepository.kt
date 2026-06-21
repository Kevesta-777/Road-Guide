package com.example.roadguideapp.goldhunt.events.achievements

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class SeasonalEventAchievementCompletionRecord(
    val entity: SeasonalEventAchievementCompletionEntity,
    val isNew: Boolean,
)

internal class SeasonalEventAchievementCompletionRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext)
        .seasonalEventAchievementCompletionDao()
    private val writeMutex = Mutex()

    suspend fun isCompletionGranted(eventId: String): Boolean =
        dao.findEventId(eventId) != null

    suspend fun recordCompletion(
        entity: SeasonalEventAchievementCompletionEntity,
    ): SeasonalEventAchievementCompletionRecord = writeMutex.withLock {
        if (dao.findEventId(entity.eventId) != null) {
            return@withLock SeasonalEventAchievementCompletionRecord(entity = entity, isNew = false)
        }
        val inserted = dao.insert(entity)
        SeasonalEventAchievementCompletionRecord(
            entity = entity,
            isNew = inserted >= 0L,
        )
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventAchievementCompletionRepository? = null

        fun get(context: Context): SeasonalEventAchievementCompletionRepository =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventAchievementCompletionRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
