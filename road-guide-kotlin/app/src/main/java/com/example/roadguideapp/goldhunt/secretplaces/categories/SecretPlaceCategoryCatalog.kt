package com.example.roadguideapp.goldhunt.secretplaces.categories

/**
 * Read-only access to [SecretPlaceCategory] definitions.
 * Placement, discovery, and reward systems consume this catalog; no generation here.
 */
internal object SecretPlaceCategoryCatalog {
    val displayOrder: List<SecretPlaceCategory> = SecretPlaceCategory.ALL_ORDERED

    fun fromId(id: String): SecretPlaceCategory? = SecretPlaceCategory.fromId(id)

    fun totalRarityWeight(): Int = displayOrder.sumOf { it.rarityWeight }

    fun difficultyOf(category: SecretPlaceCategory): Int = category.difficultyLevel

    fun rewardMultiplierOf(category: SecretPlaceCategory): Double = category.rewardMultiplier
}
