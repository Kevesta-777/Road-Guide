package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace

/**
 * Builds dispatch-ready outcomes from [SecretPlaceCategoryRewards] bundles.
 * Credits flow through [CreditGrant]; XP and hook keys are exposed for future unlockers.
 */
internal object SecretPlaceCategoryRewardDispatcher {
    fun dispatchDiscovery(place: SecretPlace): SecretPlaceCategoryRewardOutcome =
        dispatch(place, SecretPlaceCategoryRewardPhase.DISCOVERY)

    fun dispatchCompletion(place: SecretPlace): SecretPlaceCategoryRewardOutcome =
        dispatch(place, SecretPlaceCategoryRewardPhase.COMPLETION)

    fun discoveryGrant(place: SecretPlace): CreditGrant? =
        dispatchDiscovery(place).creditGrant

    fun completionGrant(place: SecretPlace): CreditGrant? =
        dispatchCompletion(place).creditGrant

    private fun dispatch(
        place: SecretPlace,
        phase: SecretPlaceCategoryRewardPhase,
    ): SecretPlaceCategoryRewardOutcome {
        val bundle = when (phase) {
            SecretPlaceCategoryRewardPhase.DISCOVERY ->
                SecretPlaceCategoryRewards.discoveryBundle(place)
            SecretPlaceCategoryRewardPhase.COMPLETION ->
                SecretPlaceCategoryRewards.completionBundle(place)
        }
        return SecretPlaceCategoryRewardOutcome(
            phase = phase,
            place = place,
            bundle = bundle,
            creditGrant = buildCreditGrant(place, bundle, phase),
        )
    }

    private fun buildCreditGrant(
        place: SecretPlace,
        bundle: SecretPlaceCategoryRewardBundle,
        phase: SecretPlaceCategoryRewardPhase,
    ): CreditGrant? {
        if (bundle.credits <= 0) return null
        return CreditGrant(
            eventId = "credit:secret_place_category:${phase.id}:${place.secretPlaceId}",
            ruleType = RewardRuleType.SECRET_PLACE_CATEGORY,
            amount = bundle.credits,
            label = place.category.displayName,
        )
    }
}
