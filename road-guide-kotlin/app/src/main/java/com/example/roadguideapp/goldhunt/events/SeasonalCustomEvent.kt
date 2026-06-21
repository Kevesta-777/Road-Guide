package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Runtime custom seasonal event instance (parallel to catalog [SeasonalEvent]).
 */
internal data class SeasonalCustomEvent(
    val eventId: String,
    val eventKey: String,
    val displayName: String,
    val active: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rewardMultiplier: Double,
    val eventSeed: String,
    val minExplorerLevel: Int,
    val achievementKey: String,
    val storyFragmentKey: String,
    val legendaryRelicKey: String,
    val schemaVersion: Int = SeasonalEventSchema.VERSION,
    val extensionJson: String = SeasonalEventSchema.EMPTY_EXTENSIONS_JSON,
) {
    val participationEventId: String
        get() = SeasonalEventSchema.CustomEventIds.participation(
            eventKey,
            startDate.toEpochDay().toString(),
        )

    val explorerUnlockAchievementKey: String
        get() = SeasonalEventSchema.ExplorerLevelKeys.customUnlocked(eventKey)

    init {
        require(eventId.isNotBlank()) { "eventId must not be blank" }
        require(eventKey.isNotBlank()) { "eventKey must not be blank" }
        require(!endDate.isBefore(startDate)) { "endDate must be on or after startDate" }
        require(rewardMultiplier >= SeasonalEventSchema.BASE_MULTIPLIER) {
            "rewardMultiplier must be >= ${SeasonalEventSchema.BASE_MULTIPLIER}"
        }
        require(eventSeed.isNotBlank()) { "eventSeed must not be blank" }
        require(minExplorerLevel >= 1) { "minExplorerLevel must be at least 1" }
    }

    fun isActiveOn(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    fun withActiveFlag(evaluatedAt: LocalDate): SeasonalCustomEvent =
        copy(active = isActiveOn(evaluatedAt))
}
