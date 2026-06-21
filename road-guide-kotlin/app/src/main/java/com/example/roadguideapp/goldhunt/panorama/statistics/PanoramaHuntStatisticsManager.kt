package com.example.roadguideapp.goldhunt.panorama.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalRepository
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository

/**
 * Persists lifetime panorama hunt statistics on the explorer profile singleton.
 */
internal class PanoramaHuntStatisticsManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val repository = ExplorerProfileRepository.get(appContext)
    private val sessionRepository = PanoramaHuntSessionRepository.get(appContext)
    private val journalRepository = PanoramaHuntJournalRepository.get(appContext)

    suspend fun load(): PanoramaHuntStatistics =
        PanoramaHuntStatisticsMapper.fromProfile(repository.loadProfile())

    suspend fun recordHuntStarted(
        hunt: PanoramaHunt,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntStatistics {
        val session = sessionRepository.recordStarted(hunt, timestampMs)
        if (!session.isNew) {
            return load()
        }
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = PanoramaHuntStatisticsMapper.fromProfile(profile).withHuntStarted()
            PanoramaHuntStatisticsMapper.applyToProfile(profile, stats)
        }
        journalRepository.recordDiscovery(hunt, timestampMs)
        return PanoramaHuntStatisticsMapper.fromProfile(updated)
    }

    suspend fun recordTargetFound(
        targetType: PanoramaHiddenTargetType,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = PanoramaHuntStatisticsMapper
                .fromProfile(profile)
                .withTargetFound(targetType)
            PanoramaHuntStatisticsMapper.applyToProfile(profile, stats)
        }
        return PanoramaHuntStatisticsMapper.fromProfile(updated)
    }

    suspend fun recordHuntCompletion(
        creditsEarned: Int,
        xpEarned: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = PanoramaHuntStatisticsMapper
                .fromProfile(profile)
                .withHuntCompletion(creditsEarned, xpEarned)
            PanoramaHuntStatisticsMapper.applyToProfile(profile, stats)
        }
        return PanoramaHuntStatisticsMapper.fromProfile(updated)
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntStatisticsManager? = null

        fun get(context: Context): PanoramaHuntStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
