package com.example.roadguideapp.goldhunt.panorama.interaction

import com.example.roadguideapp.panorama.interaction.PanoramaViewState

/**
 * Screen tap plus viewer orientation snapshot for hunt interaction.
 */
internal data class PanoramaHuntTapEvent(
    val screenX: Float,
    val screenY: Float,
    val viewState: PanoramaViewState,
)
