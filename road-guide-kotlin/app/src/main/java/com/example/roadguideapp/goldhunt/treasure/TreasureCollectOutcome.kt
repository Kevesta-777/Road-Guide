package com.example.roadguideapp.goldhunt.treasure

internal sealed class TreasureCollectOutcome {
    data class Collected(
        val credits: Int,
        val type: TreasureType,
        val clusterCompletionCredits: Int = 0,
        val clusterCompleted: Boolean = false,
    ) : TreasureCollectOutcome()

    data object AlreadyCollected : TreasureCollectOutcome()

    data object NotReady : TreasureCollectOutcome()
}
