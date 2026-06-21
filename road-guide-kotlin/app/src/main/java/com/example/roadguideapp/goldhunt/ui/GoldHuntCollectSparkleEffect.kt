package com.example.roadguideapp.goldhunt.ui

import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.maplibre.android.geometry.LatLng

internal data class GoldHuntCollectSparkleEffect(
    val latLng: LatLng,
    val style: GoldHuntCollectSparkleStyle,
    val treasureType: TreasureType? = null,
    val showMarkerPop: Boolean = false,
)

internal object GoldHuntCollectEffectTiming {
    /** Treasure icon 1x -> 2x pop; kept inside the sparkle window. */
    const val POP_DURATION_MS = 360
    const val SPARKLE_DURATION_MS = 480
    const val TOTAL_DURATION_MS = SPARKLE_DURATION_MS
    /** When sparkle progress passes this, the popped marker begins fading out. */
    const val MARKER_FADE_START_PROGRESS = 0.45f
}

internal object GoldHuntCollectSparkleVisuals {
    const val SIZE_SCALE = 1.42f
    const val PARTICLE_COUNT = 18
    const val TINY_STAR_COUNT = 56
}
