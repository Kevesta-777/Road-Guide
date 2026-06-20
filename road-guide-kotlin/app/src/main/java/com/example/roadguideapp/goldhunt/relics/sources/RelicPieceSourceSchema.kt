package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType

/**
 * Schema metadata for idempotent relic piece source grants.
 */
internal object RelicPieceSourceSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object HookTypes {
        const val RELIC_PIECE = "relicPiece"
    }

    object EventPrefixes {
        const val GRANT = "relic_piece_source:grant"
    }

    object ExtensionKeys {
        const val SOURCE_TYPE = "sourceType"
        const val SOURCE_KEY = "sourceKey"
        const val RELIC_ID = "relicId"
        const val PIECE_NUMBER = "pieceNumber"
        const val DOMAIN_ID = "domainId"
    }

    fun grantEventId(
        sourceType: RelicPieceSourceType,
        sourceKey: String,
        pieceId: String,
        domainId: String? = null,
    ): String {
        val domainSuffix = domainId?.trim()?.takeIf { it.isNotEmpty() }?.let { ":$it" }.orEmpty()
        return "${EventPrefixes.GRANT}:${sourceType.id}:$sourceKey:$pieceId$domainSuffix"
    }

    fun hookKey(pieceId: String): String = pieceId
}
