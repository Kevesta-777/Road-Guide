package com.example.roadguideapp.goldhunt.treasure.metadata

import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Metadata model for a treasure instance (storage and reward wiring only; no UI).
 *
 * [baseCreditReward] and [baseXpReward] are canonical baselines from [TreasureDefinitionCatalog],
 * not rolled procedural credit amounts from generators.
 */
internal data class TreasureDefinition(
    val treasureId: String,
    val treasureType: TreasureType,
    val rarity: TreasureRarity,
    val baseCreditReward: Int,
    val baseXpReward: Long,
    val catalogKey: TreasureDefinitionKey,
    val category: TreasureDefinitionCategory = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
    val displayName: String,
    val schemaVersion: Int = TreasureDefinitionSchema.VERSION,
    val extensionJson: String = TreasureDefinitionSchema.EMPTY_EXTENSIONS_JSON,
)
