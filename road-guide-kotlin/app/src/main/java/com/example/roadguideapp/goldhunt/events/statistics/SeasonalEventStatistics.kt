package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardGrantResult

/**
 * Lifetime seasonal event statistics persisted on the explorer profile.
 */
internal data class SeasonalEventStatistics(
    val eventsJoined: Int = 0,
    val eventsCompleted: Int = 0,
    val rewardsEarned: Int = 0,
    val bestEventType: SeasonalEventType? = null,
    val favoriteEventType: SeasonalEventType? = null,
    val treasuresCollected: Int = 0,
    val xpEarned: Long = 0L,
    val creditsEarned: Int = 0,
    val fragmentsEarned: Int = 0,
) {
    fun withTreasureGrant(
        isFirstJoin: Boolean,
        grantResult: SeasonalEventRewardGrantResult,
        completedCycleDelta: Int,
        allProgress: List<SeasonalEventProgress>,
    ): SeasonalEventStatistics {
        val fragmentDelta = if (grantResult.hooks.storyFragmentRecorded) 1 else 0
        val hadReward = grantResult.creditsGranted > 0 ||
            grantResult.xpGranted > 0L ||
            fragmentDelta > 0
        val next = copy(
            eventsJoined = eventsJoined + if (isFirstJoin) 1 else 0,
            eventsCompleted = eventsCompleted + completedCycleDelta.coerceAtLeast(0),
            rewardsEarned = rewardsEarned + if (hadReward) 1 else 0,
            treasuresCollected = treasuresCollected + 1,
            xpEarned = xpEarned + grantResult.xpGranted.coerceAtLeast(0L),
            creditsEarned = creditsEarned + grantResult.creditsGranted.coerceAtLeast(0),
            fragmentsEarned = fragmentsEarned + fragmentDelta,
        )
        return next.withDerivedEventTypes(allProgress)
    }

    fun withDerivedEventTypes(allProgress: List<SeasonalEventProgress>): SeasonalEventStatistics =
        copy(
            bestEventType = SeasonalEventStatisticsCalculator.resolveBestEventType(allProgress),
            favoriteEventType = SeasonalEventStatisticsCalculator.resolveFavoriteEventType(allProgress),
        )

    companion object {
        val EMPTY = SeasonalEventStatistics()
    }
}
