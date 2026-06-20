package com.example.roadguideapp.goldhunt.discovery

import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion

internal object GoldHuntFogVisibility {
    fun isInPlayRegion(
        lat: Double,
        lng: Double,
        region: PlayRegion,
    ): Boolean = GridIndex.encode(lat, lng, level = 0, region) != null
}
