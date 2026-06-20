package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory

internal data class HiddenRelicPresentation(
    val relicId: String,
    val name: String,
    val description: String,
    val category: RelicCategory,
    val categoryLabel: String,
    val visibility: RelicVisibility,
    val revealed: Boolean,
    val piecesCollected: Int,
    val pieceCount: Int,
    val revealedAtMs: Long?,
) {
    val isHidden: Boolean get() = visibility == RelicVisibility.HIDDEN
    val isMasked: Boolean get() = isHidden && !revealed
}

internal object HiddenRelicPresentationMapper {
    fun present(
        relic: LegendaryRelic,
        visibility: RelicVisibility,
        revealedAtMs: Long? = null,
    ): HiddenRelicPresentation {
        val revealed = HiddenRelicRevealEvaluator.isRevealed(
            visibility = visibility,
            relic = relic,
            revealedAtMs = revealedAtMs,
        )
        return if (revealed) {
            HiddenRelicPresentation(
                relicId = relic.relicId,
                name = relic.name,
                description = relic.description,
                category = relic.category,
                categoryLabel = relic.category.displayName,
                visibility = visibility,
                revealed = true,
                piecesCollected = relic.piecesCollected,
                pieceCount = relic.pieceCount,
                revealedAtMs = revealedAtMs,
            )
        } else {
            HiddenRelicPresentation(
                relicId = relic.relicId,
                name = HiddenRelicDiscoverySchema.Placeholders.HIDDEN_NAME,
                description = HiddenRelicDiscoverySchema.Placeholders.HIDDEN_DESCRIPTION,
                category = relic.category,
                categoryLabel = HiddenRelicDiscoverySchema.Placeholders.HIDDEN_CATEGORY_LABEL,
                visibility = visibility,
                revealed = false,
                piecesCollected = 0,
                pieceCount = relic.pieceCount,
                revealedAtMs = null,
            )
        }
    }
}
