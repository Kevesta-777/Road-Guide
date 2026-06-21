package com.example.roadguideapp.goldhunt.panorama

/**
 * Read-only access to [PanoramaHuntType] definitions.
 * Hunt assignment, rewards, and panorama viewers consume this catalog; no generation here.
 */
internal object PanoramaHuntTypeCatalog {
    val displayOrder: List<PanoramaHuntType> = PanoramaHuntType.ALL_ORDERED

    fun fromId(id: String): PanoramaHuntType? = PanoramaHuntType.fromId(id)

    fun difficultyOf(type: PanoramaHuntType): Int = type.difficulty

    fun rewardMultiplierOf(type: PanoramaHuntType): Double = type.rewardMultiplier
}
