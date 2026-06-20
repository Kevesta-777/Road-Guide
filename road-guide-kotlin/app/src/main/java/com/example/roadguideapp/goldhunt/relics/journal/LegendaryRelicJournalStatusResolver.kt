package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainContext
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainResolver
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility

internal object LegendaryRelicJournalStatusResolver {
    fun resolve(
        relic: LegendaryRelic,
        visibility: RelicVisibility,
        revealed: Boolean,
        explorerLevel: Int,
        chainContext: RelicHuntChainContext? = null,
    ): LegendaryRelicJournalStatus = when {
        relic.completed -> LegendaryRelicJournalStatus.COMPLETED
        chainContext != null && !RelicHuntChainResolver.isProgressAllowed(relic.relicId, chainContext) ->
            LegendaryRelicJournalStatus.LOCKED
        explorerLevel < RelicSchema.CategoryGates.minExplorerLevel(relic.category) ->
            LegendaryRelicJournalStatus.LOCKED
        visibility == RelicVisibility.HIDDEN && !revealed ->
            LegendaryRelicJournalStatus.HIDDEN_MASKED
        relic.piecesCollected <= 0 -> LegendaryRelicJournalStatus.MISSING
        else -> LegendaryRelicJournalStatus.IN_PROGRESS
    }
}
