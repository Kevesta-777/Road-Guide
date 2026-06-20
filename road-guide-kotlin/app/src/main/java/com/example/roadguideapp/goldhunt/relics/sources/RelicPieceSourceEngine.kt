package com.example.roadguideapp.goldhunt.relics.sources

/**
 * Central engine that resolves gameplay source events into relic piece grant operations.
 *
 * Register new provenance by extending [RelicPieceSourceEvent] and
 * [RelicPieceSourceKeyResolver]; piece definitions are supplied by [RelicPieceSourceIndex].
 */
internal class RelicPieceSourceEngine(
    private val index: RelicPieceSourceIndex = RelicPieceSourceIndex.fromRegistry(),
) {
    fun resolve(
        event: RelicPieceSourceEvent,
        discoveredPieceIds: Set<String> = emptySet(),
    ): RelicPieceSourceEngineResult {
        val resolutionResult = RelicPieceSourceResolver.resolve(
            event = event,
            index = index,
            discoveredPieceIds = discoveredPieceIds,
        )
        return RelicPieceSourceEngineResult(
            resolutions = resolutionResult.resolutions,
            grants = emptyList(),
            skippedAlreadyDiscovered = resolutionResult.skippedAlreadyDiscovered,
        )
    }

    fun resolveAndDispatch(
        event: RelicPieceSourceEvent,
        discoveredPieceIds: Set<String> = emptySet(),
    ): RelicPieceSourceEngineResult {
        val resolutionResult = RelicPieceSourceResolver.resolve(
            event = event,
            index = index,
            discoveredPieceIds = discoveredPieceIds,
        )
        val grants = RelicPieceSourceDispatcher.dispatch(resolutionResult.resolutions)
        return RelicPieceSourceEngineResult(
            resolutions = resolutionResult.resolutions,
            grants = grants,
            skippedAlreadyDiscovered = resolutionResult.skippedAlreadyDiscovered,
        )
    }

    fun resolveBatch(
        events: List<RelicPieceSourceEvent>,
        discoveredPieceIds: Set<String> = emptySet(),
    ): List<RelicPieceSourceEngineResult> =
        events.map { event ->
            resolveAndDispatch(
                event = event,
                discoveredPieceIds = discoveredPieceIds,
            )
        }

    companion object {
        fun create(index: RelicPieceSourceIndex = RelicPieceSourceIndex.fromRegistry()): RelicPieceSourceEngine =
            RelicPieceSourceEngine(index = index)
    }
}

internal data class RelicPieceSourceEngineResult(
    val resolutions: List<RelicPieceSourceResolution>,
    val grants: List<RelicPieceSourceGrantOperation>,
    val skippedAlreadyDiscovered: Int = 0,
) {
    val hasGrants: Boolean get() = grants.isNotEmpty()
}
