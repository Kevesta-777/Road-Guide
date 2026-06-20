package com.example.roadguideapp.goldhunt.secretplaces

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Explorer-level access checks for secret places.
 * Does not award XP or mutate profile state.
 */
internal object SecretPlaceExplorerLevelGate {
    fun isAccessible(place: SecretPlace, explorerLevel: Int): Boolean {
        val required = place.resolvedMinimumExplorerLevel
        return explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL) >= required
    }

    fun levelsUntilAccessible(place: SecretPlace, explorerLevel: Int): Int {
        val required = place.resolvedMinimumExplorerLevel
        val current = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return (required - current).coerceAtLeast(0)
    }
}
