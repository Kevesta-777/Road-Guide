package com.example.roadguideapp.goldhunt.relics

/**
 * Player-facing relic piece progress model for the unified Gold Hunt catalog.
 *
 * Tracks whether an individual shard has been discovered and when. Pieces roll up
 * into parent [LegendaryRelic] completion via [piecesCollected].
 */
internal data class RelicPiece(
    val pieceId: String,
    val relicId: String,
    val pieceNumber: Int,
    val totalPieces: Int,
    val sourceType: RelicPieceSourceType,
    val discovered: Boolean,
    val discoveryDate: Long?,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(pieceId.isNotBlank()) { "pieceId must not be blank" }
        require(relicId.isNotBlank()) { "relicId must not be blank" }
        require(pieceNumber >= 1) { "pieceNumber must be at least 1" }
        require(totalPieces >= 1) { "totalPieces must be at least 1" }
        require(pieceNumber <= totalPieces) { "pieceNumber must not exceed totalPieces" }
        require(!discovered || discoveryDate != null) {
            "discovered pieces must record discoveryDate"
        }
        require(discovered || discoveryDate == null) {
            "undiscovered pieces must not record discoveryDate"
        }
    }

    val isUndiscovered: Boolean get() = !discovered

    fun withDiscovery(
        discovered: Boolean,
        discoveryDate: Long? = null,
    ): RelicPiece {
        val resolvedDiscovered = discovered
        val resolvedDiscoveryDate = when {
            resolvedDiscovered -> discoveryDate ?: this.discoveryDate ?: System.currentTimeMillis()
            else -> null
        }
        return copy(
            discovered = resolvedDiscovered,
            discoveryDate = resolvedDiscoveryDate,
        )
    }
}
