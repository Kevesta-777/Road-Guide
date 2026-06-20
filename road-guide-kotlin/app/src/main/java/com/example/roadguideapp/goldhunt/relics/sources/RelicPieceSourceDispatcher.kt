package com.example.roadguideapp.goldhunt.relics.sources

/**
 * Converts resolved relic piece matches into idempotent grant operations.
 */
internal object RelicPieceSourceDispatcher {
    fun dispatch(
        resolutions: List<RelicPieceSourceResolution>,
    ): List<RelicPieceSourceGrantOperation> =
        resolutions.map { resolution ->
            val definition = resolution.pieceDefinition
            RelicPieceSourceGrantOperation(
                pieceId = definition.pieceId,
                relicId = definition.relicId,
                pieceNumber = definition.pieceNumber,
                sourceType = resolution.sourceType,
                sourceKey = resolution.sourceKey,
                domainId = resolution.domainId,
                hookKey = RelicPieceSourceSchema.hookKey(definition.pieceId),
                eventId = RelicPieceSourceSchema.grantEventId(
                    sourceType = resolution.sourceType,
                    sourceKey = resolution.sourceKey,
                    pieceId = definition.pieceId,
                    domainId = resolution.domainId,
                ),
                grantedAtMs = resolution.event.timestampMs,
            )
        }
}

internal data class RelicPieceSourceGrantOperation(
    val pieceId: String,
    val relicId: String,
    val pieceNumber: Int,
    val sourceType: com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType,
    val sourceKey: String,
    val domainId: String,
    val hookKey: String,
    val eventId: String,
    val grantedAtMs: Long,
)
