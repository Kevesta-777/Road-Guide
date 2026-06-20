package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Encyclopedia view model for one catalog treasure family.
 */
internal data class TreasureEncyclopediaEntry(
    val catalogKey: TreasureDefinitionKey,
    val displayName: String,
    val status: TreasureEncyclopediaStatus,
    val catalogRarity: TreasureRarity,
    val firstDiscoveredAtMs: Long?,
    val bestRarity: TreasureRarity?,
    val totalCreditsEarned: Int,
    val collectionCount: Int,
    val enabled: Boolean,
    val schemaVersion: Int = TreasureEncyclopediaSchema.VERSION,
    val extensionJson: String = TreasureEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isFound: Boolean get() = status == TreasureEncyclopediaStatus.FOUND
    val isMissing: Boolean get() = status == TreasureEncyclopediaStatus.MISSING
    val isLocked: Boolean get() = status == TreasureEncyclopediaStatus.LOCKED

    /** Rarity shown in UI: best collected tier, else catalog default when missing. */
    val displayRarity: TreasureRarity get() = bestRarity ?: catalogRarity
}

internal data class TreasureEncyclopediaProgress(
    val foundCount: Int,
    val trackableCount: Int,
    val lockedCount: Int,
    val totalCreditsEarned: Int,
) {
    val completionFraction: Float
        get() = if (trackableCount <= 0) 0f else foundCount.toFloat() / trackableCount.toFloat()
}

internal data class TreasureEncyclopedia(
    val entries: List<TreasureEncyclopediaEntry>,
    val progress: TreasureEncyclopediaProgress,
    val schemaVersion: Int = TreasureEncyclopediaSchema.VERSION,
)
