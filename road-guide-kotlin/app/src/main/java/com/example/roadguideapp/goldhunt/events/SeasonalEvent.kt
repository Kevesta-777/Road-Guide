package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Runtime seasonal event instance for one catalog type and cycle year.
 *
 * Coordinates procedural content via [eventSeed] without exposing target coordinates.
 * Achievement and story-fragment hooks are stable per [eventType].
 */
internal data class SeasonalEvent(
    val eventId: String,
    val eventType: SeasonalEventType,
    val active: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rewardMultiplier: Double,
    val eventSeed: String,
    val cycleYear: Int,
    val minExplorerLevel: Int = eventType.minExplorerLevel,
    val achievementKey: String = SeasonalEventSchema.AchievementKeys.firstParticipation(eventType),
    val storyFragmentKey: String = SeasonalEventSchema.StoryFragmentKeys.forType(eventType),
    val legendaryRelicKey: String = SeasonalEventSchema.LegendaryRelicKeys.forType(eventType),
    val schemaVersion: Int = SeasonalEventSchema.VERSION,
) {
    val displayName: String
        get() = eventType.displayName

    val participationEventId: String
        get() = SeasonalEventSchema.EventIds.participation(eventType, cycleYear)

    val explorerUnlockAchievementKey: String
        get() = SeasonalEventSchema.ExplorerLevelKeys.unlocked(eventType)

    init {
        require(eventId.isNotBlank()) { "eventId must not be blank" }
        require(!endDate.isBefore(startDate)) { "endDate must be on or after startDate" }
        require(rewardMultiplier >= SeasonalEventSchema.BASE_MULTIPLIER) {
            "rewardMultiplier must be >= ${SeasonalEventSchema.BASE_MULTIPLIER}"
        }
        require(eventSeed.isNotBlank()) { "eventSeed must not be blank" }
        require(cycleYear >= 1970) { "cycleYear must be plausible" }
        require(minExplorerLevel >= 1) { "minExplorerLevel must be at least 1" }
    }

    fun isActiveOn(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    fun withActiveFlag(evaluatedAt: LocalDate): SeasonalEvent =
        copy(active = isActiveOn(evaluatedAt))
}
