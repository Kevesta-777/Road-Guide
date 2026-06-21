package com.example.roadguideapp.goldhunt.panorama.targets

/**
 * A single hidden target placed on an equirectangular panorama sphere.
 *
 * @param bearing Horizontal angle in degrees (0–360), aligned with [com.example.roadguideapp.panorama.gl.PanoramaRenderer.yaw].
 * @param pitch Vertical look angle in degrees, aligned with [com.example.roadguideapp.panorama.gl.PanoramaRenderer.pitch].
 * @param radius Angular hit radius in degrees — player view must be within this cone to collect.
 */
internal data class PanoramaHiddenTarget(
    val targetId: String,
    val huntId: String,
    val slotIndex: Int,
    val bearing: Float,
    val pitch: Float,
    val radius: Float,
    val targetType: PanoramaHiddenTargetType,
    val isAnimated: Boolean = false,
    val isLegendary: Boolean = false,
    val schemaVersion: Int = PanoramaHiddenTargetSchema.VERSION,
    val extensionJson: String = PanoramaHiddenTargetSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(targetId.isNotBlank()) { "targetId must not be blank" }
        require(huntId.isNotBlank()) { "huntId must not be blank" }
        require(slotIndex >= 0) { "slotIndex must be non-negative" }
        require(bearing in PanoramaHiddenTargetSchema.MIN_BEARING..PanoramaHiddenTargetSchema.MAX_BEARING) {
            "bearing out of range"
        }
        require(pitch in PanoramaHiddenTargetSchema.MIN_PITCH..PanoramaHiddenTargetSchema.MAX_PITCH) {
            "pitch out of range"
        }
        require(radius in PanoramaHiddenTargetSchema.MIN_RADIUS..PanoramaHiddenTargetSchema.MAX_RADIUS) {
            "radius out of range"
        }
        if (isLegendary) {
            require(targetType == PanoramaHiddenTargetType.LEGENDARY_RELIC) {
                "legendary targets must use LEGENDARY_RELIC type"
            }
        }
    }
}
