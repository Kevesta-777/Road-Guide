package com.example.roadguideapp.goldhunt.panorama

/**
 * Offline tuning for [PanoramaHuntGenerator].
 * Bump [GENERATOR_VERSION] when roll logic changes so hunts can be regenerated consistently.
 */
internal object PanoramaHuntGenerationConfig {
    const val GENERATOR_VERSION = 1

    /** Spawn weights for [PanoramaHuntTypePicker] (sum = 1.0). Easier hunts are more common. */
    const val WEIGHT_HIDDEN_SYMBOL = 0.32
    const val WEIGHT_OBJECT_HUNT = 0.26
    const val WEIGHT_PANORAMA_PUZZLE = 0.22
    const val WEIGHT_SECRET_CODE = 0.12
    const val WEIGHT_RELIC_HUNT = 0.08
}
