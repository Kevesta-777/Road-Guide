package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

/**
 * Inputs for deterministic secret-place category assignment.
 *
 * [anchorId] should be a stable location key (e.g. procedural secretPlaceId).
 * [latitude] and [longitude] are quantized inside the engine seed for repeatability.
 */
internal data class SecretPlaceCategoryAssignmentInput(
    val anchorId: String,
    val playRegionId: String,
    val latitude: Double,
    val longitude: Double,
    val poiMetadata: SecretPlacePoiMetadata = SecretPlacePoiMetadata.EMPTY,
    val environmentType: SecretPlaceEnvironmentType = SecretPlaceEnvironmentType.UNKNOWN,
    val treasureDensity: SecretPlaceTreasureDensity,
    val explorationProgress: SecretPlaceExplorationProgress,
) {
    init {
        require(anchorId.isNotBlank()) { "anchorId must not be blank" }
        require(playRegionId.isNotBlank()) { "playRegionId must not be blank" }
        require(latitude in -90.0..90.0) { "latitude out of range" }
        require(longitude in -180.0..180.0) { "longitude out of range" }
    }

    companion object {
        fun forGridAnchor(
            anchorId: String,
            playRegionId: String,
            latitude: Double,
            longitude: Double,
            l1CellId: String,
            l2CellId: String,
            poiMetadata: SecretPlacePoiMetadata = SecretPlacePoiMetadata.EMPTY,
            environmentType: SecretPlaceEnvironmentType = SecretPlaceEnvironmentType.UNKNOWN,
        ): SecretPlaceCategoryAssignmentInput = SecretPlaceCategoryAssignmentInput(
            anchorId = anchorId,
            playRegionId = playRegionId,
            latitude = latitude,
            longitude = longitude,
            poiMetadata = poiMetadata,
            environmentType = environmentType,
            treasureDensity = SecretPlaceTreasureDensity.fromGridCell(l1CellId),
            explorationProgress = SecretPlaceExplorationProgress.fromGridCells(
                l1CellId = l1CellId,
                l2CellId = l2CellId,
                playRegionId = playRegionId,
            ),
        )
    }
}
