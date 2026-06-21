package com.example.roadguideapp.goldhunt.relics.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceEngine
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceEvent
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryManager
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantManager
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatisticsManager
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryHookRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central legendary relic progress engine.
 *
 * Seeds catalog rows from [com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry],
 * resolves source events into piece grants, prevents duplicate discoveries via hook rows,
 * and mirrors aggregate statistics onto the explorer profile.
 */
internal class LegendaryRelicProgressEngine private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val repository = LegendaryRelicProgressRepository.get(appContext)
    private val statisticsManager = LegendaryRelicStatisticsManager.get(appContext)
    private val rewardGrantManager = LegendaryRelicRewardGrantManager.get(appContext)
    private val hiddenDiscoveryManager = HiddenRelicDiscoveryManager.get(appContext)
    private val sourceEngine = RelicPieceSourceEngine.create()
    private val storyHookRepository = LegendaryRelicStoryHookRepository.get(appContext)
    private val reconcileMutex = Mutex()

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()) {
        repository.ensureSeeded(timestampMs)
        statisticsManager.reconcile(timestampMs)
        rewardGrantManager.reconcilePendingGrants(timestampMs)
        hiddenDiscoveryManager.ensureSeeded(timestampMs)
    }

    suspend fun loadProgress(): LegendaryRelicProgress {
        ensureSeeded()
        return repository.loadProgress()
    }

    suspend fun handle(event: RelicPieceSourceEvent): LegendaryRelicProgressBatchResult =
        reconcileMutex.withLock {
            ensureSeeded(event.timestampMs)
            val discoveredPieceIds = repository.loadDiscoveredPieceIds()
            val sourceResult = sourceEngine.resolveAndDispatch(
                event = event,
                discoveredPieceIds = discoveredPieceIds,
            )
            val grantResults = repository.applyGrants(sourceResult.grants)
            val rewardResults = grantResults
                .filter { it.relicCompleted }
                .mapNotNull { grantResult ->
                    grantResult.relicId?.let { relicId ->
                        rewardGrantManager.tryGrantRewardsForRelicId(
                            relicId = relicId,
                            timestampMs = event.timestampMs,
                        )
                    }
                }
            grantResults.forEach { grantResult ->
                storyHookRepository.recordPieceStoryHooks(
                    grantResult = grantResult,
                    timestampMs = event.timestampMs,
                )
            }
            val hiddenRevealResults = grantResults.map { grantResult ->
                hiddenDiscoveryManager.tryRevealOnPieceGrant(
                    grantResult = grantResult,
                    timestampMs = event.timestampMs,
                )
            }
            if (
                grantResults.any { it.isNewGrant } ||
                rewardResults.any { it.isNewGrant } ||
                hiddenRevealResults.any { it.isNewReveal }
            ) {
                statisticsManager.reconcile(event.timestampMs)
            }
            LegendaryRelicProgressBatchResult(
                grants = grantResults,
                progress = repository.loadProgress(),
                rewardGrants = rewardResults,
                hiddenReveals = hiddenRevealResults.filter { it.isNewReveal },
            )
        }

    suspend fun handleAll(events: List<RelicPieceSourceEvent>): List<LegendaryRelicProgressBatchResult> =
        events.map { handle(it) }

    companion object {
        @Volatile
        private var instance: LegendaryRelicProgressEngine? = null

        fun get(context: Context): LegendaryRelicProgressEngine =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicProgressEngine(context.applicationContext)
                    .also { instance = it }
            }
    }
}
