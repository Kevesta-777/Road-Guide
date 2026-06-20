package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEventActivationEngine
import com.example.roadguideapp.goldhunt.events.SeasonalEventDeviceClock
import com.example.roadguideapp.goldhunt.events.SeasonalEventLevelGate
import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureDetector
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityResolver
import java.time.LocalDate

internal object SeasonalEventRewardDispatcher {
    fun dispatchTreasureCollection(
        spec: TreasureSpec,
        explorerLevel: Int,
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
    ): SeasonalEventRewardOutcome? {
        val eventType = EventTreasureDetector.eventTypeFrom(spec) ?: return null
        val activation = SeasonalEventActivationEngine.activate(evaluatedAt)
        val event = activation.activeCatalogEvents.firstOrNull { it.eventType == eventType }
            ?: return null
        if (!SeasonalEventLevelGate.isUnlocked(event, explorerLevel)) return null

        val context = SeasonalEventRewardContext(
            spec = spec,
            event = event,
            explorerLevel = explorerLevel,
            rarity = TreasureRarityResolver.resolve(spec),
        )
        val bundle = SeasonalEventRewards.treasureCollectionBundle(context)
        return SeasonalEventRewardOutcome(
            spec = spec,
            event = event,
            bundle = bundle,
            creditGrant = buildCreditGrant(spec, event, bundle),
            explorerLevel = explorerLevel,
            scaling = SeasonalEventRewardScaling.breakdown(context),
        )
    }

    private fun buildCreditGrant(
        spec: TreasureSpec,
        event: com.example.roadguideapp.goldhunt.events.SeasonalEvent,
        bundle: SeasonalEventRewardBundle,
    ): CreditGrant = CreditGrant(
        eventId = SeasonalEventSchema.EventIds.rewardGrant(
            type = event.eventType,
            grantKey = SeasonalEventRewardSchema.GrantKeys.TREASURE_COLLECTION,
            year = event.cycleYear,
        ) + ":${spec.treasureId}",
        ruleType = RewardRuleType.SEASONAL_EVENT_TREASURE,
        amount = bundle.credits,
        label = spec.treasureId,
    )
}
