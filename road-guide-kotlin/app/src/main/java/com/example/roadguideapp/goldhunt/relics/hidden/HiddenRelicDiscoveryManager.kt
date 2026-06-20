package com.example.roadguideapp.goldhunt.relics.hidden

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.progress.AchievementProgressEngine
import com.example.roadguideapp.goldhunt.relics.progress.LegendaryRelicProgressGrantResult
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatisticsManager

/**
 * Orchestrates hidden relic revelation on first piece discovery and fans out to
 * achievements, story-fragment hooks, and explorer profile statistics.
 */
internal class HiddenRelicDiscoveryManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val repository = HiddenRelicDiscoveryRepository.get(appContext)
    private val statisticsManager = LegendaryRelicStatisticsManager.get(appContext)
    private val achievementProgressEngine = AchievementProgressEngine.get(appContext)

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()) {
        repository.ensureSeeded(timestampMs)
        statisticsManager.reconcile(timestampMs)
    }

    private suspend fun syncStatistics(
        revealResult: HiddenRelicDiscoveryRevealResult,
        timestampMs: Long,
    ) {
        statisticsManager.recordHiddenReveal(revealResult, timestampMs)
        statisticsManager.reconcile(timestampMs)
    }

    suspend fun loadProgress(): HiddenRelicDiscoveryProgress {
        ensureSeeded()
        return repository.loadProgress()
    }

    suspend fun tryRevealOnPieceGrant(
        grantResult: LegendaryRelicProgressGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): HiddenRelicDiscoveryRevealResult {
        if (!grantResult.isNewGrant) return HiddenRelicDiscoveryRevealResult.skipped
        val relicId = grantResult.relicId ?: return HiddenRelicDiscoveryRevealResult.skipped
        val pieceId = grantResult.pieceId ?: return HiddenRelicDiscoveryRevealResult.skipped
        if (grantResult.piecesCollected < 1) return HiddenRelicDiscoveryRevealResult.skipped

        val revealResult = repository.revealOnFirstPiece(
            relicId = relicId,
            pieceId = pieceId,
            timestampMs = timestampMs,
        )
        if (!revealResult.isNewReveal) return revealResult

        revealResult.achievementKey?.let { achievementKey ->
            if (revealResult.achievementRecorded) {
                achievementProgressEngine.recordLegacyIncrement(
                    achievementKey = achievementKey,
                    increment = 1,
                    timestampMs = timestampMs,
                )
            }
        }
        syncStatistics(revealResult, timestampMs)
        return revealResult
    }

    companion object {
        @Volatile
        private var instance: HiddenRelicDiscoveryManager? = null

        fun get(context: Context): HiddenRelicDiscoveryManager =
            instance ?: synchronized(this) {
                instance ?: HiddenRelicDiscoveryManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
