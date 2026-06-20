package com.example.roadguideapp.goldhunt

internal object GoldHuntMapZoom {
    fun treasuresVisibleAt(zoom: Double): Boolean =
        zoom.isFinite() && zoom >= GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM

    fun secretsVisibleAt(zoom: Double): Boolean =
        zoom.isFinite() && zoom >= GoldHuntConfig.SECRET_DISPLAY_MIN_ZOOM
}
