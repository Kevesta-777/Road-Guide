package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.relics.RelicPieceDefinition

/**
 * Resolves source events into the next collectible relic piece for each matching relic.
 */
internal object RelicPieceSourceResolver {
    fun resolve(
        event: RelicPieceSourceEvent,
        index: RelicPieceSourceIndex,
        discoveredPieceIds: Set<String> = emptySet(),
    ): RelicPieceSourceResolutionResult {
        val keyMatches = RelicPieceSourceKeyResolver.resolve(event)
        if (keyMatches.isEmpty()) {
            return RelicPieceSourceResolutionResult()
        }

        val candidateDefinitions = linkedMapOf<String, CandidateMatch>()
        var skippedAlreadyDiscovered = 0

        keyMatches.forEach { keyMatch ->
            val definitions = index.lookup(keyMatch.sourceType, keyMatch.sourceKey)
            definitions.forEach { definition ->
                if (definition.pieceId in discoveredPieceIds) {
                    skippedAlreadyDiscovered += 1
                    return@forEach
                }
                val existing = candidateDefinitions[definition.relicId]
                if (existing == null || definition.pieceNumber < existing.definition.pieceNumber) {
                    candidateDefinitions[definition.relicId] = CandidateMatch(
                        definition = definition,
                        keyMatch = keyMatch,
                    )
                }
            }
        }

        val resolutions = candidateDefinitions.values
            .sortedWith(compareBy({ it.definition.relicId }, { it.definition.pieceNumber }))
            .map { candidate ->
                RelicPieceSourceResolution(
                    pieceDefinition = candidate.definition,
                    sourceType = candidate.keyMatch.sourceType,
                    sourceKey = candidate.keyMatch.sourceKey,
                    domainId = candidate.keyMatch.domainId,
                    event = event,
                )
            }

        return RelicPieceSourceResolutionResult(
            resolutions = resolutions,
            skippedAlreadyDiscovered = skippedAlreadyDiscovered,
        )
    }

    private data class CandidateMatch(
        val definition: RelicPieceDefinition,
        val keyMatch: RelicPieceSourceKeyMatch,
    )
}

internal data class RelicPieceSourceResolutionResult(
    val resolutions: List<RelicPieceSourceResolution> = emptyList(),
    val skippedAlreadyDiscovered: Int = 0,
)

internal data class RelicPieceSourceResolution(
    val pieceDefinition: RelicPieceDefinition,
    val sourceType: com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType,
    val sourceKey: String,
    val domainId: String,
    val event: RelicPieceSourceEvent,
)
