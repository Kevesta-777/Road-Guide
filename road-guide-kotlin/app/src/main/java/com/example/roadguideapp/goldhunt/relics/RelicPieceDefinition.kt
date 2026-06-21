package com.example.roadguideapp.goldhunt.relics

/**
 * Canonical relic piece definition for the unified catalog layer.
 *
 * Each piece binds to a [sourceType] and [sourceKey] that future domain unlockers
 * (secret places, achievements, story fragments, panorama hunts) can match idempotently.
 */
internal data class RelicPieceDefinition(
    val pieceId: String,
    val relicId: String,
    val pieceNumber: Int,
    val totalPieces: Int,
    val sourceType: RelicPieceSourceType,
    val sourceKey: String,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(pieceId.isNotBlank()) { "pieceId must not be blank" }
        require(relicId.isNotBlank()) { "relicId must not be blank" }
        require(pieceNumber >= 1) { "pieceNumber must be at least 1" }
        require(totalPieces >= 1) { "totalPieces must be at least 1" }
        require(pieceNumber <= totalPieces) { "pieceNumber must not exceed totalPieces" }
        require(sourceKey.isNotBlank()) { "sourceKey must not be blank" }
    }
}
