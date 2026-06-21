package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator

/**
 * Stable world seeds for offline treasure rolls.
 * [fromPlayRegion] ties rarity to the active play-region bounds (survives app restarts).
 */
internal object TreasureWorldSeed {
    const val DEFAULT: String = "goldhunt:world:v${GoldHuntConfig.TREASURE_GENERATOR_VERSION}"

    fun fromPlayRegion(region: PlayRegion): String =
        "${DEFAULT}:${TreasureGenerator.playRegionId(region)}"
}
