package com.example.roadguideapp.goldhunt.panorama

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Explorer-level access checks for panorama hunts.
 * Does not award XP or mutate profile state.
 */
internal object PanoramaHuntExplorerLevelGate {
    fun isAccessible(hunt: PanoramaHunt, explorerLevel: Int): Boolean {
        val required = hunt.resolvedMinimumExplorerLevel
        return explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL) >= required
    }

    fun levelsUntilAccessible(hunt: PanoramaHunt, explorerLevel: Int): Int {
        val required = hunt.resolvedMinimumExplorerLevel
        val current = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        return (required - current).coerceAtLeast(0)
    }
}
