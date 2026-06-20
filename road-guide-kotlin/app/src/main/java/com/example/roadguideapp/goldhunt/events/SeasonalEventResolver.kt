package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Builds [SeasonalEventContext] for gameplay systems (rewards, achievements, generators).
 */
internal object SeasonalEventResolver {
    fun resolve(
        date: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): SeasonalEventContext = resolve(
        activation = SeasonalEventActivationEngine.activate(date),
        explorerLevel = explorerLevel,
    )

    fun resolve(
        activation: SeasonalEventActivationResult,
        explorerLevel: Int? = null,
    ): SeasonalEventContext {
        if (!activation.hasActiveEvents) {
            return SeasonalEventContext.INACTIVE.copy(
                evaluatedAt = activation.evaluatedAt,
                explorerLevel = explorerLevel,
            )
        }
        val accessibleCatalog = if (explorerLevel != null) {
            SeasonalEventLevelGate.filterAccessible(
                activation.activeCatalogEvents,
                explorerLevel,
            )
        } else {
            activation.activeCatalogEvents
        }
        val accessibleCustom = if (explorerLevel != null) {
            SeasonalEventLevelGate.filterAccessibleCustom(
                activation.activeCustomEvents,
                explorerLevel,
            )
        } else {
            activation.activeCustomEvents
        }
        val multiplier = combinedMultiplier(accessibleCatalog, accessibleCustom)
        return SeasonalEventContext(
            evaluatedAt = activation.evaluatedAt,
            activeEvents = activation.activeCatalogEvents,
            activeCustomEvents = activation.activeCustomEvents,
            combinedMultiplier = multiplier,
            primaryEvent = accessibleCatalog.maxByOrNull { it.rewardMultiplier },
            primaryCustomEvent = accessibleCustom.maxByOrNull { it.rewardMultiplier },
            explorerLevel = explorerLevel,
        )
    }

    fun combinedMultiplier(events: List<SeasonalEvent>): Double {
        if (events.isEmpty()) return SeasonalEventSchema.BASE_MULTIPLIER
        return events.maxOf { it.rewardMultiplier }
    }

    fun combinedMultiplier(
        catalogEvents: List<SeasonalEvent>,
        customEvents: List<SeasonalCustomEvent>,
    ): Double {
        val multipliers = buildList {
            addAll(catalogEvents.map { it.rewardMultiplier })
            addAll(customEvents.map { it.rewardMultiplier })
        }
        return multipliers.maxOrNull() ?: SeasonalEventSchema.BASE_MULTIPLIER
    }

    fun scaleAmount(baseAmount: Long, context: SeasonalEventContext): Long {
        if (baseAmount <= 0L || !context.hasActiveEvents) return baseAmount
        if (context.accessibleEvents.isEmpty() && context.accessibleCustomEvents.isEmpty()) {
            return baseAmount
        }
        return (baseAmount * context.combinedMultiplier).toLong().coerceAtLeast(baseAmount)
    }

    fun scaleAmount(baseAmount: Int, context: SeasonalEventContext): Int {
        if (baseAmount <= 0 || !context.hasActiveEvents) return baseAmount
        if (context.accessibleEvents.isEmpty() && context.accessibleCustomEvents.isEmpty()) {
            return baseAmount
        }
        return (baseAmount * context.combinedMultiplier).toInt().coerceAtLeast(baseAmount)
    }
}
