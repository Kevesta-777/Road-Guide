package com.example.roadguideapp.goldhunt.treasure.metadata

import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Canonical metadata template for a treasure family (no instance [treasureId]).
 */
internal data class TreasureTypeTemplate(
    val key: TreasureDefinitionKey,
    val displayName: String,
    val treasureType: TreasureType?,
    val rarity: TreasureRarity,
    val baseCreditReward: Int,
    val baseXpReward: Long,
    val category: TreasureDefinitionCategory,
    val enabled: Boolean = true,
)
