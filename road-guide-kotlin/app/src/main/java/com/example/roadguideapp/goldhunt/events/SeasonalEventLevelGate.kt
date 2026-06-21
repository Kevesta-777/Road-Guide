package com.example.roadguideapp.goldhunt.events

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Explorer-level access checks for seasonal event instances.
 */
internal object SeasonalEventLevelGate {
    fun isUnlocked(event: SeasonalEvent, explorerLevel: Int): Boolean =
        isUnlocked(event.minExplorerLevel, explorerLevel)

    fun isUnlocked(event: SeasonalCustomEvent, explorerLevel: Int): Boolean =
        isUnlocked(event.minExplorerLevel, explorerLevel)

    fun isUnlocked(minExplorerLevel: Int, explorerLevel: Int): Boolean {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return level >= minExplorerLevel
    }

    fun isUnlocked(type: SeasonalEventType, explorerLevel: Int): Boolean {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return level >= type.minExplorerLevel
    }

    fun levelsUntilUnlocked(event: SeasonalEvent, explorerLevel: Int): Int {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return (event.minExplorerLevel - level).coerceAtLeast(0)
    }

    fun filterUnlocked(
        events: List<SeasonalEvent>,
        explorerLevel: Int,
    ): List<SeasonalEvent> = events.filter { isUnlocked(it, explorerLevel) }

    fun filterAccessible(
        events: List<SeasonalEvent>,
        explorerLevel: Int,
    ): List<SeasonalEvent> = events.filter { it.active && isUnlocked(it, explorerLevel) }

    fun filterAccessibleCustom(
        events: List<SeasonalCustomEvent>,
        explorerLevel: Int,
    ): List<SeasonalCustomEvent> = events.filter { it.active && isUnlocked(it, explorerLevel) }

    fun unlockedTypes(explorerLevel: Int): List<SeasonalEventType> =
        SeasonalEventCatalog.displayOrder.filter { isUnlocked(it, explorerLevel) }
}
