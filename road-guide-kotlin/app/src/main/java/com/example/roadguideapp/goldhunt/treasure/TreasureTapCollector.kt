package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

internal object TreasureTapCollector {
    /** Collect when the player taps a visible treasure marker (explicit intent). */
    suspend fun collectOnTap(
        repository: GoldHuntRepository,
        spec: TreasureSpec,
        timestampMs: Long = System.currentTimeMillis(),
    ): TreasureCollectOutcome {
        repository.ensureInitialized()
        if (repository.isTreasureCollectedCached(spec.treasureId)) {
            return TreasureCollectOutcome.AlreadyCollected
        }
        val tick = repository.collectTreasure(spec, timestampMs)
        if (!tick.newlyExplored) {
            return TreasureCollectOutcome.AlreadyCollected
        }
        return TreasureCollectOutcome.Collected(
            credits = tick.creditsEarned,
            type = spec.type,
            clusterCompletionCredits = tick.clusterCompletionCredits,
            clusterCompleted = tick.clusterCompleted,
        )
    }
}
