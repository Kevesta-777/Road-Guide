package com.example.roadguideapp.goldhunt.events.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SeasonalEventProgressRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).seasonalEventProgressDao()
    private val writeMutex = Mutex()

    suspend fun load(eventId: String): SeasonalEventProgress? =
        writeMutex.withLock {
            dao.getByEventId(eventId)?.let { SeasonalEventProgressMapper.toDomain(it) }
        }

    suspend fun loadAll(): List<SeasonalEventProgress> =
        writeMutex.withLock {
            dao.getAll().mapNotNull { SeasonalEventProgressMapper.toDomain(it) }
        }

    suspend fun save(
        progress: SeasonalEventProgress,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventProgress = writeMutex.withLock {
        val entity = SeasonalEventProgressMapper.toEntity(
            progress.copy(updatedAtMs = timestampMs),
        )
        dao.upsert(entity)
        SeasonalEventProgressMapper.toDomain(entity) ?: progress.copy(updatedAtMs = timestampMs)
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventProgressRepository? = null

        fun get(context: Context): SeasonalEventProgressRepository =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventProgressRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
