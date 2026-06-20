package com.example.roadguideapp.goldhunt.profile.xp

import com.example.roadguideapp.goldhunt.treasure.TreasureType

/** Canonical gameplay XP amounts (offline tuning). */
internal object GameplayXpValues {
    const val ROAD_DISCOVERY = 1L
    const val AREA_DISCOVERY = 2L
    const val SECRET_PLACE = 100L

    const val TREASURE_STAR = 3L
    const val TREASURE_FLOWER = 5L
    const val TREASURE_CRYSTAL = 10L
    const val TREASURE_GIFT = 25L

    fun forTreasureType(type: TreasureType): Long? = when (type) {
        TreasureType.STAR -> TREASURE_STAR
        TreasureType.FLOWER -> TREASURE_FLOWER
        TreasureType.CRYSTAL -> TREASURE_CRYSTAL
        TreasureType.GIFT -> TREASURE_GIFT
        TreasureType.HINT -> null
    }
}
