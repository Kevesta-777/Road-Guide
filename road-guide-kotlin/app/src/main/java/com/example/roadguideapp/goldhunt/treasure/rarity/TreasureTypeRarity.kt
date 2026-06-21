package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.treasure.TreasureType

/**
 * Read-only mapping from existing [TreasureType] values to default [TreasureRarity] tiers.
 * Does not alter [com.example.roadguideapp.goldhunt.treasure.TreasureTypePicker] or generators.
 */
internal object TreasureTypeRarity {
    fun rarityFor(type: TreasureType): TreasureRarity = when (type) {
        TreasureType.STAR -> TreasureRarity.COMMON
        TreasureType.FLOWER -> TreasureRarity.UNCOMMON
        TreasureType.CRYSTAL -> TreasureRarity.RARE
        TreasureType.GIFT -> TreasureRarity.EPIC
        TreasureType.HINT -> TreasureRarity.COMMON
    }

    fun rarityForTypeId(typeId: String): TreasureRarity? =
        TreasureType.fromId(typeId)?.let { rarityFor(it) }
}
