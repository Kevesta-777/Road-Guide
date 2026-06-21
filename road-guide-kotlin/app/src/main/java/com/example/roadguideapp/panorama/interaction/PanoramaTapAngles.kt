package com.example.roadguideapp.panorama.interaction

/**
 * Sphere angles derived from a screen tap.
 *
 * [bearing] aligns with [com.example.roadguideapp.panorama.gl.PanoramaRenderer.yaw].
 * [pitch] aligns with [com.example.roadguideapp.panorama.gl.PanoramaRenderer.pitch].
 */
data class PanoramaTapAngles(
    val bearing: Float,
    val pitch: Float,
)
