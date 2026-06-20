package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal object SeasonalEventStatisticsMapper {
    fun fromEntity(entity: ExplorerProfileEntity): SeasonalEventStatistics =
        SeasonalEventStatistics(
            eventsJoined = entity.seasonalEventStatsEventsJoined.coerceAtLeast(0),
            eventsCompleted = entity.seasonalEventStatsCompletedCycles.coerceAtLeast(0),
            rewardsEarned = entity.seasonalEventStatsRewardsEarned.coerceAtLeast(0),
            bestEventType = entity.seasonalEventStatsBestEventType?.let(SeasonalEventType::fromId),
            favoriteEventType = entity.seasonalEventStatsFavoriteEventType?.let(SeasonalEventType::fromId),
            treasuresCollected = entity.seasonalEventStatsTreasuresCollected.coerceAtLeast(0),
            xpEarned = entity.seasonalEventStatsXpEarned.coerceAtLeast(0L),
            creditsEarned = entity.seasonalEventStatsCreditsEarned.coerceAtLeast(0),
            fragmentsEarned = entity.seasonalEventStatsFragmentsEarned.coerceAtLeast(0),
        )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: SeasonalEventStatistics,
    ): ExplorerProfileEntity = entity.copy(
        seasonalEventStatsEventsJoined = stats.eventsJoined.coerceAtLeast(0),
        seasonalEventStatsCompletedCycles = stats.eventsCompleted.coerceAtLeast(0),
        seasonalEventStatsRewardsEarned = stats.rewardsEarned.coerceAtLeast(0),
        seasonalEventStatsBestEventType = stats.bestEventType?.id,
        seasonalEventStatsFavoriteEventType = stats.favoriteEventType?.id,
        seasonalEventStatsTreasuresCollected = stats.treasuresCollected.coerceAtLeast(0),
        seasonalEventStatsXpEarned = stats.xpEarned.coerceAtLeast(0L),
        seasonalEventStatsCreditsEarned = stats.creditsEarned.coerceAtLeast(0),
        seasonalEventStatsFragmentsEarned = stats.fragmentsEarned.coerceAtLeast(0),
    )
}
