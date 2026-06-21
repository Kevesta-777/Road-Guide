package com.example.roadguideapp.goldhunt.panorama

/**
 * Canonical inputs for deterministic panorama hunt generation.
 *
 * @param panoramaId Stable panorama asset id from [com.example.roadguideapp.map.PlacePanoramaClient].
 * @param secretPlaceId Linked secret-place instance id.
 * @param worldSeed Offline world seed (use [PanoramaHuntWorldSeed.fromPlayRegion]).
 */
internal data class PanoramaHuntGenerationInput(
    val panoramaId: String,
    val secretPlaceId: String,
    val worldSeed: String,
) {
    init {
        require(panoramaId.isNotBlank()) { "panoramaId must not be blank" }
        require(secretPlaceId.isNotBlank()) { "secretPlaceId must not be blank" }
        require(worldSeed.isNotBlank()) { "worldSeed must not be blank" }
    }
}
