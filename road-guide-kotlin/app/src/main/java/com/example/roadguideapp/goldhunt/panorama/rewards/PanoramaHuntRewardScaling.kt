package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntCompletionConfig
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Scales panorama hunt completion rewards by difficulty, hunt type, and explorer level.
 */
internal object PanoramaHuntRewardScaling {
    /** +3% credits/XP per explorer level above 1, capped at +100%. */
    const val EXPLORER_LEVEL_BONUS_PER_LEVEL = 0.03
    const val MAX_EXPLORER_LEVEL_BONUS = 1.0

    fun combinedMultiplier(context: PanoramaHuntRewardContext): Double {
        val difficultyFactor = context.hunt.difficulty.coerceAtLeast(1).toDouble()
        val typeFactor = context.hunt.effectiveRewardMultiplier
        val levelFactor = explorerLevelBonus(context.explorerLevel)
        return difficultyFactor * typeFactor * levelFactor
    }

    fun explorerLevelBonus(explorerLevel: Int): Double {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        val bonus = (level - 1) * EXPLORER_LEVEL_BONUS_PER_LEVEL
        return 1.0 + bonus.coerceAtMost(MAX_EXPLORER_LEVEL_BONUS)
    }

    fun scaledCredits(context: PanoramaHuntRewardContext): Int {
        val multiplier = combinedMultiplier(context)
        return (PanoramaHuntCompletionConfig.BASE_COMPLETION_CREDITS * multiplier)
            .roundToLong()
            .toInt()
            .coerceAtLeast(1)
    }

    fun scaledXp(context: PanoramaHuntRewardContext): Long {
        val multiplier = combinedMultiplier(context)
        return (PanoramaHuntCompletionConfig.BASE_COMPLETION_XP * multiplier)
            .roundToLong()
            .coerceAtLeast(1L)
    }

    fun breakdown(context: PanoramaHuntRewardContext): PanoramaHuntRewardScalingBreakdown {
        val hunt = context.hunt
        return PanoramaHuntRewardScalingBreakdown(
            difficultyFactor = hunt.difficulty.coerceAtLeast(1).toDouble(),
            huntTypeFactor = hunt.effectiveRewardMultiplier,
            explorerLevelFactor = explorerLevelBonus(context.explorerLevel),
            combinedMultiplier = combinedMultiplier(context),
        )
    }
}

internal data class PanoramaHuntRewardScalingBreakdown(
    val difficultyFactor: Double,
    val huntTypeFactor: Double,
    val explorerLevelFactor: Double,
    val combinedMultiplier: Double,
)
