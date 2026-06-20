package com.example.roadguideapp.goldhunt.radar.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RadarRewardStatisticsRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).radarRewardStatisticsDao()
    private val writeMutex = Mutex()

    suspend fun load(): RadarRewardStatistics =
        writeMutex.withLock {
            val entity = dao.get()
            if (entity != null) {
                return@withLock RadarRewardStatisticsMapper.toDomain(entity)
            }
            val created = RadarRewardStatistics.empty()
            dao.upsert(RadarRewardStatisticsMapper.toEntity(created))
            created
        }

    suspend fun save(
        stats: RadarRewardStatistics,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarRewardStatistics = writeMutex.withLock {
        val entity = RadarRewardStatisticsMapper.toEntity(
            stats.copy(updatedAtMs = timestampMs),
        )
        dao.upsert(entity)
        RadarRewardStatisticsMapper.toDomain(entity)
    }

    companion object {
        @Volatile
        private var instance: RadarRewardStatisticsRepository? = null

        fun get(context: Context): RadarRewardStatisticsRepository =
            instance ?: synchronized(this) {
                instance ?: RadarRewardStatisticsRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
