package com.example.roadguideapp.offlinegraph

internal data class OfflineGraphProgress(
    val phase: OfflineGraphEngine.ImportPhase,
    val detail: String,
    val percent: Int?,
    val elapsedMs: Long,
)

internal data class CopyProgress(
    val detail: String,
    val percent: Int?,
)
