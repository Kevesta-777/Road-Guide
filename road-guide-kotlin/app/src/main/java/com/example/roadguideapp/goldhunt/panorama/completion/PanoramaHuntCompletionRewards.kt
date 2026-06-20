package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewards
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

internal object PanoramaHuntCompletionRewards {
    fun creditsFor(
        hunt: PanoramaHunt,
        explorerLevel: Int = ExplorerLevelCalculator.MIN_LEVEL,
    ): Int = PanoramaHuntRewards.completionBundle(hunt, explorerLevel).credits

    fun xpFor(
        hunt: PanoramaHunt,
        explorerLevel: Int = ExplorerLevelCalculator.MIN_LEVEL,
    ): Long = PanoramaHuntRewards.completionBundle(hunt, explorerLevel).xp
}
