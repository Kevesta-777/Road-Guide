package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.interaction.PanoramaHuntInteractionLayer
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet

/**
 * Loaded hunt state for the panorama viewer (progress + interaction).
 */
internal data class PanoramaHuntSession(
    val hunt: PanoramaHunt,
    val targets: PanoramaHiddenTargetSet,
    val interaction: PanoramaHuntInteractionLayer,
)
