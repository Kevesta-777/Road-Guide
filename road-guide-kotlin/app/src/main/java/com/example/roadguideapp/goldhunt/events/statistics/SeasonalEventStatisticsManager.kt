package com.example.roadguideapp.goldhunt.events.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressRepository
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository

/**
 * Loads and backfills lifetime seasonal event statistics on the explorer profile.
 */
internal class SeasonalEventStatisticsManager private constructor(context: Context) {
    private val explorerProfileRepository = ExplorerProfileRepository.get(context.applicationContext)
    private val progressRepository = SeasonalEventProgressRepository.get(context.applicationContext)

    suspend fun load(): SeasonalEventStatistics {
        val profile = explorerProfileRepository.loadProfile()
        var stats = profile.seasonalEventStatistics
        val allProgress = progressRepository.loadAll()
        val needsBackfill = stats.bestEventType == null && stats.favoriteEventType == null &&
            allProgress.any { it.treasuresCollected > 0 }
        if (needsBackfill) {
            stats = stats.withDerivedEventTypes(allProgress)
            explorerProfileRepository.updateProfile { current ->
                current.copy(seasonalEventStatistics = stats)
            }
        }
        return stats
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventStatisticsManager? = null

        fun get(context: Context): SeasonalEventStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
