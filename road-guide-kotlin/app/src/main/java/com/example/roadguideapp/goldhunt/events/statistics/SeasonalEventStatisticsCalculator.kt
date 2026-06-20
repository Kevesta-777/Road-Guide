package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress

internal object SeasonalEventStatisticsCalculator {
    private const val FRAGMENT_REWARD_WEIGHT = 50L

    internal data class TypeAggregate(
        val eventType: SeasonalEventType,
        val cyclesJoined: Int,
        val treasuresCollected: Int,
        val creditsEarned: Int,
        val xpEarned: Long,
        val fragmentsEarned: Int,
        val maxCompletionPercent: Double,
    ) {
        fun rewardScore(): Long =
            creditsEarned.toLong() + xpEarned + fragmentsEarned * FRAGMENT_REWARD_WEIGHT
    }

    fun aggregatesFrom(progress: List<SeasonalEventProgress>): List<TypeAggregate> =
        progress
            .groupBy { it.eventType }
            .map { (eventType, rows) ->
                TypeAggregate(
                    eventType = eventType,
                    cyclesJoined = rows.count { it.treasuresCollected > 0 },
                    treasuresCollected = rows.sumOf { it.treasuresCollected },
                    creditsEarned = rows.sumOf { it.creditsEarned },
                    xpEarned = rows.sumOf { it.xpEarned },
                    fragmentsEarned = rows.sumOf { it.fragmentsEarned },
                    maxCompletionPercent = rows.maxOfOrNull { it.completionPercent } ?: 0.0,
                )
            }

    fun resolveBestEventType(progress: List<SeasonalEventProgress>): SeasonalEventType? =
        aggregatesFrom(progress)
            .filter { it.rewardScore() > 0L }
            .maxWithOrNull(
                compareBy<TypeAggregate> { it.rewardScore() }
                    .thenBy { it.maxCompletionPercent }
                    .thenBy { it.eventType.ordinal },
            )
            ?.eventType

    fun resolveFavoriteEventType(progress: List<SeasonalEventProgress>): SeasonalEventType? =
        aggregatesFrom(progress)
            .filter { it.treasuresCollected > 0 }
            .maxWithOrNull(
                compareBy<TypeAggregate> { it.treasuresCollected }
                    .thenBy { it.cyclesJoined }
                    .thenBy { it.eventType.ordinal },
            )
            ?.eventType
}
