package com.example.roadguideapp.goldhunt.relics

internal object RelicPieceMapper {
    fun toDomain(entity: RelicPieceEntity): RelicPiece? {
        val sourceType = RelicPieceSourceType.fromId(entity.sourceType) ?: return null
        return RelicPiece(
            pieceId = entity.pieceId,
            relicId = entity.relicId,
            pieceNumber = entity.pieceNumber.coerceAtLeast(1),
            totalPieces = entity.totalPieces.coerceAtLeast(1),
            sourceType = sourceType,
            discovered = entity.discovered,
            discoveryDate = entity.discoveryDateMs,
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
        )
    }

    fun toEntity(
        piece: RelicPiece,
        sourceKey: String,
        updatedAtMs: Long = System.currentTimeMillis(),
    ): RelicPieceEntity = RelicPieceEntity(
        pieceId = piece.pieceId,
        relicId = piece.relicId,
        pieceNumber = piece.pieceNumber,
        totalPieces = piece.totalPieces,
        sourceType = piece.sourceType.id,
        sourceKey = sourceKey,
        discovered = piece.discovered,
        discoveryDateMs = piece.discoveryDate,
        schemaVersion = piece.schemaVersion,
        extensionJson = piece.extensionJson,
        updatedAtMs = updatedAtMs,
    )

    fun fromDefinition(
        definition: RelicPieceDefinition,
        discovered: Boolean = false,
        discoveryDate: Long? = null,
    ): RelicPiece {
        val resolvedDiscovered = discovered
        val resolvedDiscoveryDate = when {
            resolvedDiscovered -> discoveryDate ?: System.currentTimeMillis()
            else -> null
        }
        return RelicPiece(
            pieceId = definition.pieceId,
            relicId = definition.relicId,
            pieceNumber = definition.pieceNumber,
            totalPieces = definition.totalPieces,
            sourceType = definition.sourceType,
            discovered = resolvedDiscovered,
            discoveryDate = resolvedDiscoveryDate,
            schemaVersion = definition.schemaVersion,
            extensionJson = definition.extensionJson,
        )
    }
}
