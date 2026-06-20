package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpValues
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rewards.TreasureRewardMultipliers
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Scales seasonal event treasure rewards by event type, explorer level, and treasure rarity.
 */
internal object SeasonalEventRewardScaling {
    const val EXPLORER_LEVEL_BONUS_PER_LEVEL = 0.03
    const val MAX_EXPLORER_LEVEL_BONUS = 1.0

    fun combinedMultiplier(context: SeasonalEventRewardContext): Double {
        val eventFactor = context.event.rewardMultiplier
        val levelFactor = explorerLevelBonus(context.explorerLevel)
        val rarityFactor = TreasureRewardMultipliers.forRarity(context.rarity)
        return eventFactor * levelFactor * rarityFactor
    }

    fun explorerLevelBonus(explorerLevel: Int): Double {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        val bonus = (level - 1) * EXPLORER_LEVEL_BONUS_PER_LEVEL
        return 1.0 + bonus.coerceAtMost(MAX_EXPLORER_LEVEL_BONUS)
    }

    fun scaledCredits(context: SeasonalEventRewardContext): Int {
        val base = context.spec.creditAmount.coerceAtLeast(0)
        if (base <= 0) return 0
        return (base * combinedMultiplier(context))
            .roundToLong()
            .toInt()
            .coerceAtLeast(base)
    }

    fun scaledXp(context: SeasonalEventRewardContext): Long {
        val base = baseXpFor(context.spec.type)
        if (base <= 0L) return 0L
        return (base * combinedMultiplier(context))
            .roundToLong()
            .coerceAtLeast(base)
    }

    fun breakdown(context: SeasonalEventRewardContext): SeasonalEventRewardScalingBreakdown {
        val rarityFactor = TreasureRewardMultipliers.forRarity(context.rarity)
        return SeasonalEventRewardScalingBreakdown(
            eventTypeFactor = context.event.rewardMultiplier,
            explorerLevelFactor = explorerLevelBonus(context.explorerLevel),
            rarityFactor = rarityFactor,
            combinedMultiplier = combinedMultiplier(context),
        )
    }

    fun legendaryRelicProgressIncrement(rarity: com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity): Int =
        when {
            rarity.tier >= com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity.MYTHIC.tier -> 3
            rarity.tier >= com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity.LEGENDARY.tier -> 2
            rarity.tier >= SeasonalEventRewardSchema.LEGENDARY_RELIC_MIN_RARITY.tier -> 1
            else -> 0
        }

    fun qualifiesForStoryFragment(rarity: com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity): Boolean =
        rarity.tier >= SeasonalEventRewardSchema.STORY_FRAGMENT_MIN_RARITY.tier

    private fun baseXpFor(type: TreasureType): Long =
        GameplayXpValues.forTreasureType(type) ?: 0L
}

internal data class SeasonalEventRewardScalingBreakdown(
    val eventTypeFactor: Double,
    val explorerLevelFactor: Double,
    val rarityFactor: Double,
    val combinedMultiplier: Double,
)
