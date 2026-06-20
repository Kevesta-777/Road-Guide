package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Explorer-level access checks for radar types.
 * Does not award XP, persist pulse state, or render UI.
 */
internal object RadarExplorerLevelGate {
    fun isUnlocked(type: RadarType, explorerLevel: Int): Boolean {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return level >= type.unlockLevel
    }

    fun isUnlocked(template: RadarTemplate, explorerLevel: Int): Boolean =
        isUnlocked(template.type, explorerLevel)

    fun levelsUntilUnlocked(type: RadarType, explorerLevel: Int): Int {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return (type.unlockLevel - level).coerceAtLeast(0)
    }

    fun unlockedTypes(explorerLevel: Int): List<RadarType> =
        RadarCatalog.displayOrder.filter { isUnlocked(it, explorerLevel) }
}
