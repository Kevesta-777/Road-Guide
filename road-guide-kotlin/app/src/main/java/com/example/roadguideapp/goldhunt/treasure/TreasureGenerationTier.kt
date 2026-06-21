package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig

/** Which treasure types may spawn based on player collection progress. */
internal enum class TreasureGenerationTier {
    STAR_ONLY,
    STAR_AND_FLOWER,
    STAR_FLOWER_AND_CRYSTAL,
    STAR_FLOWER_CRYSTAL_AND_GIFT,
    ;

    companion object {
        fun fromProgress(
            starsCollected: Int,
            flowersCollected: Int,
            crystalsCollected: Int,
        ): TreasureGenerationTier {
            if (starsCollected < GoldHuntConfig.FLOWER_UNLOCK_MIN_STARS) return STAR_ONLY
            if (flowersCollected < GoldHuntConfig.CRYSTAL_UNLOCK_MIN_FLOWERS) {
                return STAR_AND_FLOWER
            }
            if (crystalsCollected < GoldHuntConfig.GIFT_UNLOCK_MIN_CRYSTALS) {
                return STAR_FLOWER_AND_CRYSTAL
            }
            return STAR_FLOWER_CRYSTAL_AND_GIFT
        }
    }
}
