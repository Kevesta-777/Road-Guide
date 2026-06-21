package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.rewards.CreditGrant

internal data class LegendaryRelicRewardOutcome(
    val relic: LegendaryRelic,
    val creditGrant: CreditGrant,
    val xp: Long,
    val storyFragmentKey: String?,
    val titleKey: String?,
    val badgeKey: String?,
    val cosmeticKey: String?,
) {
    fun storyFragmentEventId(): String? =
        storyFragmentKey?.let { LegendaryRelicRewardSchema.storyFragmentEventId(relic.relicId) }

    fun titleEventId(): String? =
        titleKey?.let { LegendaryRelicRewardSchema.titleEventId(relic.relicId) }

    fun badgeEventId(): String? =
        badgeKey?.let { LegendaryRelicRewardSchema.badgeEventId(relic.relicId) }

    fun cosmeticEventId(): String? =
        cosmeticKey?.let { LegendaryRelicRewardSchema.cosmeticEventId(relic.relicId, it) }
}
