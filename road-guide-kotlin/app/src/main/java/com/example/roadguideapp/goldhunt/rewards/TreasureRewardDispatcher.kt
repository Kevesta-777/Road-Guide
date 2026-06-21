package com.example.roadguideapp.goldhunt.rewards

import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import java.security.MessageDigest

internal object TreasureRewardDispatcher {
    fun grant(spec: TreasureSpec): CreditGrant? {
        if (spec.type == TreasureType.HINT || spec.creditAmount <= 0) return null
        val rule = ruleFor(spec.type)
        return CreditGrant(
            eventId = stableEventId(rule, spec.treasureId),
            ruleType = rule,
            amount = spec.creditAmount,
            label = spec.treasureId,
        )
    }

    private fun ruleFor(type: TreasureType): String = when (type) {
        TreasureType.STAR -> RewardRuleType.TREASURE_STAR
        TreasureType.FLOWER -> RewardRuleType.TREASURE_FLOWER
        TreasureType.CRYSTAL -> RewardRuleType.TREASURE_CRYSTAL
        TreasureType.GIFT -> RewardRuleType.TREASURE_GIFT
        TreasureType.HINT -> RewardRuleType.TREASURE_HINT
    }

    private fun stableEventId(rule: String, target: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$rule:$target".toByteArray())
        return bytes.take(16).joinToString("") { "%02x".format(it) }
    }
}
