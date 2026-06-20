package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig

internal data class TreasureHoardRegion(
    val regionName: String,
    val lat: Double,
    val lng: Double,
    val seed: Long,
    val treasureCount: Int = GoldHuntConfig.HOARD_TREASURE_COUNT,
)
