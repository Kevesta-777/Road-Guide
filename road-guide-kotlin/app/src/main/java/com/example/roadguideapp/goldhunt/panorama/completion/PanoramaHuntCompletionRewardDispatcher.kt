package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardDispatcher
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import com.example.roadguideapp.goldhunt.rewards.CreditGrant

internal object PanoramaHuntCompletionRewardDispatcher {
    fun grantFor(
        hunt: PanoramaHunt,
        explorerLevel: Int = ExplorerLevelCalculator.MIN_LEVEL,
    ): CreditGrant = PanoramaHuntRewardDispatcher.dispatchCompletion(hunt, explorerLevel).creditGrant
}
