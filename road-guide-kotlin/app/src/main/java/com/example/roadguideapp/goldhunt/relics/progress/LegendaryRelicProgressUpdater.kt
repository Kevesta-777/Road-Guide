package com.example.roadguideapp.goldhunt.relics.progress

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicPiece
import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition

internal object LegendaryRelicProgressUpdater {
    fun discoveredPiece(
        definition: RelicPieceDefinition,
        timestampMs: Long,
    ): RelicPiece = RelicPiece(
        pieceId = definition.pieceId,
        relicId = definition.relicId,
        pieceNumber = definition.pieceNumber,
        totalPieces = definition.totalPieces,
        sourceType = definition.sourceType,
        discovered = true,
        discoveryDate = timestampMs,
        schemaVersion = definition.schemaVersion,
        extensionJson = definition.extensionJson,
    )

    fun applyPieceDiscovery(
        relic: LegendaryRelic,
        discoveredCount: Int,
        timestampMs: Long,
    ): LegendaryRelic {
        val resolvedCount = discoveredCount.coerceIn(0, relic.pieceCount)
        val completed = resolvedCount >= relic.pieceCount
        return relic.withProgress(
            piecesCollected = resolvedCount,
            completed = completed,
            completionDate = if (completed) timestampMs else null,
        )
    }

    fun countDiscoveredPieces(
        relicId: String,
        pieces: List<RelicPiece>,
    ): Int = pieces.count { it.relicId == relicId && it.discovered }
}
