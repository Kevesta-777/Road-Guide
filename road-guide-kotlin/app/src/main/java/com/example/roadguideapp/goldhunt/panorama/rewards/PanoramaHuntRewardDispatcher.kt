package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType

internal object PanoramaHuntRewardDispatcher {
    fun dispatchCompletion(
        hunt: PanoramaHunt,
        explorerLevel: Int,
    ): PanoramaHuntRewardOutcome {
        val context = PanoramaHuntRewardContext(hunt = hunt, explorerLevel = explorerLevel)
        val bundle = PanoramaHuntRewards.completionBundle(context)
        return PanoramaHuntRewardOutcome(
            hunt = hunt,
            bundle = bundle,
            creditGrant = buildCreditGrant(hunt, bundle),
            explorerLevel = explorerLevel,
            scaling = PanoramaHuntRewardScaling.breakdown(context),
        )
    }

    private fun buildCreditGrant(
        hunt: PanoramaHunt,
        bundle: PanoramaHuntRewardBundle,
    ): CreditGrant = CreditGrant(
        eventId = "credit:panorama_hunt_complete:${hunt.huntId}",
        ruleType = RewardRuleType.PANORAMA_HUNT_COMPLETION,
        amount = bundle.credits,
        label = hunt.huntType.displayName,
    )
}
