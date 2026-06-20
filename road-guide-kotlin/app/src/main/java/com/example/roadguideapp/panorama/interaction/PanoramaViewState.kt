package com.example.roadguideapp.panorama.interaction

/**
 * Snapshot of the panorama viewer orientation used for tap-to-sphere conversion.
 */
data class PanoramaViewState(
    val yaw: Float,
    val pitch: Float,
    val fieldOfView: Float,
    val viewportAspect: Float,
    val viewportWidth: Int,
    val viewportHeight: Int,
)
