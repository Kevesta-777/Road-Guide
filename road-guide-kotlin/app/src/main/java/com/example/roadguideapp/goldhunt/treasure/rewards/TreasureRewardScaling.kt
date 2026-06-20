package com.example.roadguideapp.goldhunt.treasure.rewards

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinition
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import kotlin.math.roundToLong

/**
 * Applies rarity reward scaling: **Reward = Base × Multiplier** (credits and XP).
 *
 * Multipliers: Common 1×, Uncommon 1.5×, Rare 3×, Epic 5×, Legendary 10×, Mythic 25×.
 * Does not grant rewards — calculation only until collectors wire this in.
 */
internal object TreasureRewardScaling {
    fun scale(
        baseCredits: Int,
        baseXp: Long,
        rarity: TreasureRarity,
        achievementKey: String? = null,
        storyFragmentKey: String? = null,
        extensionJson: String = TreasureRewardScalingSchema.EMPTY_EXTENSIONS_JSON,
    ): TreasureScaledReward {
        val multiplier = TreasureRewardMultipliers.forRarity(rarity)
        return TreasureScaledReward(
            rarity = rarity,
            baseCredits = baseCredits.coerceAtLeast(0),
            baseXp = baseXp.coerceAtLeast(0L),
            rewardMultiplier = multiplier,
            scaledCredits = scaleCredits(baseCredits, multiplier),
            scaledXp = scaleXp(baseXp, multiplier),
            achievementKey = achievementKey ?: rarity.achievementKey,
            storyFragmentKey = storyFragmentKey,
            extensionJson = extensionJson,
        )
    }

    fun scale(
        definition: TreasureDefinition,
        rolledRarity: TreasureRarity,
    ): TreasureScaledReward = scale(
        baseCredits = definition.baseCreditReward,
        baseXp = definition.baseXpReward,
        rarity = rolledRarity,
        extensionJson = definition.extensionJson,
    )

    fun scaledCredits(baseCredits: Int, rarity: TreasureRarity): Int =
        scaleCredits(baseCredits, TreasureRewardMultipliers.forRarity(rarity))

    fun scaledXp(baseXp: Long, rarity: TreasureRarity): Long =
        scaleXp(baseXp, TreasureRewardMultipliers.forRarity(rarity))

    private fun scaleCredits(baseCredits: Int, multiplier: Double): Int {
        if (baseCredits <= 0) return 0
        return (baseCredits * multiplier).roundToLong().toInt().coerceAtLeast(0)
    }

    private fun scaleXp(baseXp: Long, multiplier: Double): Long {
        if (baseXp <= 0L) return 0L
        return (baseXp * multiplier).roundToLong().coerceAtLeast(0L)
    }
}
