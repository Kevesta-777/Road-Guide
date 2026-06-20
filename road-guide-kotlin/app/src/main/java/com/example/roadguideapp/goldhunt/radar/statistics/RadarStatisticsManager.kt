package com.example.roadguideapp.goldhunt.radar.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.radar.RadarScanResult

/**
 * Persists lifetime radar statistics on the explorer profile singleton.
 */
internal class RadarStatisticsManager private constructor(context: Context) {
    private val repository = ExplorerProfileRepository.get(context.applicationContext)

    suspend fun load(): RadarStatistics =
        RadarStatisticsMapper.fromProfile(repository.loadProfile())

    suspend fun recordScan(
        scanResult: RadarScanResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = RadarStatisticsMapper.fromProfile(profile).withScan(scanResult)
            RadarStatisticsMapper.applyToProfile(profile, stats)
        }
        return RadarStatisticsMapper.fromProfile(updated)
    }

    companion object {
        @Volatile
        private var instance: RadarStatisticsManager? = null

        fun get(context: Context): RadarStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: RadarStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
