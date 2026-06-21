package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType

internal object RadarRewardDispatcher {
    fun grantsFor(bundle: RadarScanRewardBundle): List<CreditGrant> {
        val grants = ArrayList<CreditGrant>(bundle.signalRewards.size + 1)
        for (reward in bundle.signalRewards) {
            grants += CreditGrant(
                eventId = reward.creditEventId,
                ruleType = RewardRuleType.RADAR_DETECTION,
                amount = reward.creditsAwarded,
                label = "${reward.targetCategory.id}:${reward.signal.targetId}",
            )
        }
        if (bundle.pulseCredits > 0) {
            grants += CreditGrant(
                eventId = bundle.pulseCreditEventId,
                ruleType = RewardRuleType.RADAR_PULSE,
                amount = bundle.pulseCredits,
                label = bundle.radarType.displayName,
            )
        }
        return grants
    }
}
