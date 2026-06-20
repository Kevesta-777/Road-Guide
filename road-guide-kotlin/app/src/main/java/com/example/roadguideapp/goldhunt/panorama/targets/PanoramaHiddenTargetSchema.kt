package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.panorama.gl.PanoramaRenderer

/**
 * Schema metadata for [PanoramaHiddenTarget] instances.
 *
 * [bearing] maps to [PanoramaRenderer.yaw] (horizontal look angle, degrees).
 * [pitch] maps to [PanoramaRenderer.pitch] (vertical look angle, degrees).
 * [radius] is the angular hit tolerance in degrees (smaller = harder to find).
 */
internal object PanoramaHiddenTargetSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    const val MIN_BEARING = 0f
    const val MAX_BEARING = 360f
    const val MIN_PITCH = -85f
    const val MAX_PITCH = 85f
    const val MIN_RADIUS = 2f
    const val MAX_RADIUS = 24f

    object ExtensionKeys {
        const val ANIMATED = "animated"
        const val LEGENDARY = "legendary"
        const val ANIMATION_PHASE = "animationPhase"
        const val LEGENDARY_RELIC_KEY = "legendaryRelicKey"
    }
}
