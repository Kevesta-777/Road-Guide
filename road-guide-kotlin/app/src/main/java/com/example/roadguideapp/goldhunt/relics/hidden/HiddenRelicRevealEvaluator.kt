package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic

/**
 * Determines when hidden relic details may be shown in player-facing surfaces.
 */
internal object HiddenRelicRevealEvaluator {
    fun isRevealed(
        visibility: RelicVisibility,
        piecesCollected: Int,
        revealedAtMs: Long? = null,
    ): Boolean = when {
        visibility != RelicVisibility.HIDDEN -> true
        revealedAtMs != null && revealedAtMs > 0L -> true
        else -> piecesCollected >= 1
    }

    fun isRevealed(
        visibility: RelicVisibility,
        relic: LegendaryRelic,
        revealedAtMs: Long? = null,
    ): Boolean = isRevealed(
        visibility = visibility,
        piecesCollected = relic.piecesCollected,
        revealedAtMs = revealedAtMs,
    )

    fun shouldMaskDetails(
        visibility: RelicVisibility,
        relic: LegendaryRelic,
        revealedAtMs: Long? = null,
    ): Boolean = visibility == RelicVisibility.HIDDEN &&
        !isRevealed(visibility, relic, revealedAtMs)
}
