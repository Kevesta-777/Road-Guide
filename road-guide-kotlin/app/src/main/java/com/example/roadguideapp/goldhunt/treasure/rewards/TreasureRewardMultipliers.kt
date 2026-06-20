package com.example.roadguideapp.goldhunt.treasure.rewards

import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Canonical reward multipliers applied to base credits and XP.
 * Separate from [TreasureRarity.rarityWeight] (spawn roll weights).
 */
internal object TreasureRewardMultipliers {
    const val COMMON = 1.0
    const val UNCOMMON = 1.5
    const val RARE = 3.0
    const val EPIC = 5.0
    const val LEGENDARY = 10.0
    const val MYTHIC = 25.0

    fun forRarity(rarity: TreasureRarity): Double = when (rarity) {
        TreasureRarity.COMMON -> COMMON
        TreasureRarity.UNCOMMON -> UNCOMMON
        TreasureRarity.RARE -> RARE
        TreasureRarity.EPIC -> EPIC
        TreasureRarity.LEGENDARY -> LEGENDARY
        TreasureRarity.MYTHIC -> MYTHIC
    }
}
