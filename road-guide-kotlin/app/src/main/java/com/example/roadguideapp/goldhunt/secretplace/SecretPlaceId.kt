package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridCell

internal object SecretPlaceId {
    fun forL2Slot(
        playRegionId: String,
        l2: GridCell,
        slot: Int,
    ): String =
        "sp:v${GoldHuntConfig.SECRET_GENERATOR_VERSION}:$playRegionId:L2:${l2.ix}:${l2.iy}:s$slot"

    fun forCatalog(catalogId: String): String =
        "sp:v${GoldHuntConfig.SECRET_GENERATOR_VERSION}:cat:$catalogId"
}
