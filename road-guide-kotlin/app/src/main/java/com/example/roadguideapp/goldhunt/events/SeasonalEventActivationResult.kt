package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Output of [SeasonalEventActivationEngine]: all events active on the evaluated device date.
 */
internal data class SeasonalEventActivationResult(
    val evaluatedAt: LocalDate,
    val activeCatalogEvents: List<SeasonalEvent>,
    val activeCustomEvents: List<SeasonalCustomEvent>,
) {
    val hasActiveEvents: Boolean
        get() = activeCatalogEvents.isNotEmpty() || activeCustomEvents.isNotEmpty()

    val activeEventCount: Int
        get() = activeCatalogEvents.size + activeCustomEvents.size

    val primaryCatalogEvent: SeasonalEvent?
        get() = activeCatalogEvents.maxByOrNull { it.rewardMultiplier }

    val primaryCustomEvent: SeasonalCustomEvent?
        get() = activeCustomEvents.maxByOrNull { it.rewardMultiplier }

    val combinedRewardMultiplier: Double
        get() {
            val multipliers = buildList {
                addAll(activeCatalogEvents.map { it.rewardMultiplier })
                addAll(activeCustomEvents.map { it.rewardMultiplier })
            }
            return multipliers.maxOrNull() ?: SeasonalEventSchema.BASE_MULTIPLIER
        }

    companion object {
        val INACTIVE = SeasonalEventActivationResult(
            evaluatedAt = LocalDate.EPOCH,
            activeCatalogEvents = emptyList(),
            activeCustomEvents = emptyList(),
        )
    }
}
