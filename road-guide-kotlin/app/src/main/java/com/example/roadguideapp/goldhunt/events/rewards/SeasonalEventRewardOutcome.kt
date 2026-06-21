package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec

internal data class SeasonalEventRewardOutcome(
    val spec: TreasureSpec,
    val event: SeasonalEvent,
    val bundle: SeasonalEventRewardBundle,
    val creditGrant: CreditGrant,
    val explorerLevel: Int,
    val scaling: SeasonalEventRewardScalingBreakdown,
) {
    val credits: Int get() = bundle.credits
    val xp: Long get() = bundle.xp

    val baseEventId: String
        get() = "${SeasonalEventRewardSchema.EventPrefixes.TREASURE}:${spec.treasureId}"

    fun creditEventId(): String = creditGrant.eventId

    fun xpEventId(): String = "$baseEventId:xp"

    fun participationEventId(): String = event.participationEventId

    fun storyFragmentEventId(): String? =
        if (!bundle.hasStoryFragment) {
            null
        } else {
            "${SeasonalEventRewardSchema.EventPrefixes.STORY}:${event.eventId}:${bundle.storyFragmentKey}"
        }

    fun achievementEventId(): String? =
        if (!bundle.hasAchievement) {
            null
        } else {
            "${SeasonalEventRewardSchema.EventPrefixes.ACHIEVEMENT}:${event.eventId}:${bundle.achievementKey}"
        }

    fun legendaryRelicEventId(): String? =
        if (!bundle.hasLegendaryRelic) {
            null
        } else {
            "${SeasonalEventRewardSchema.EventPrefixes.RELIC}:${spec.treasureId}:${bundle.legendaryRelicKey}"
        }
}
