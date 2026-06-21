package com.example.roadguideapp.goldhunt.treasure.rewards

import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Result of base reward × rarity multiplier.
 * [achievementKey] and [storyFragmentKey] are reserved for future unlock hooks.
 */
internal data class TreasureScaledReward(
    val rarity: TreasureRarity,
    val baseCredits: Int,
    val baseXp: Long,
    val rewardMultiplier: Double,
    val scaledCredits: Int,
    val scaledXp: Long,
    val achievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val extensionJson: String = TreasureRewardScalingSchema.EMPTY_EXTENSIONS_JSON,
    val schemaVersion: Int = TreasureRewardScalingSchema.VERSION,
)
