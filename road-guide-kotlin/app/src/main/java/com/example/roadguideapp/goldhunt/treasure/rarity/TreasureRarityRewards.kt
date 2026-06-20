package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.treasure.rewards.TreasureRewardScaling

/**
 * @deprecated Use [TreasureRewardScaling] directly. Kept for call-site compatibility.
 */
internal object TreasureRarityRewards {
    fun scaledCredits(baseCredits: Int, rarity: TreasureRarity): Int =
        TreasureRewardScaling.scaledCredits(baseCredits, rarity)

    fun scaledXp(baseXp: Long, rarity: TreasureRarity): Long =
        TreasureRewardScaling.scaledXp(baseXp, rarity)
}
