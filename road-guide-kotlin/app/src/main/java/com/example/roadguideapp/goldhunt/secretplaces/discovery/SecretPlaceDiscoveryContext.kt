package com.example.roadguideapp.goldhunt.secretplaces.discovery

/**
 * Runtime player/context signals used to evaluate discovery requirements.
 * Populated by GPS loops, treasure collection, clue progress, and profile state.
 */
internal data class SecretPlaceDiscoveryContext(
    val playerLat: Double,
    val playerLng: Double,
    val visitCount: Int = 0,
    val nearbyTreasuresCollected: Int = 0,
    val clueStepsCompleted: Int = 0,
    val activeClueChainKey: String? = null,
    val explorerLevel: Int = 1,
    val secretPlacesUnlocked: Boolean = true,
) {
    init {
        require(playerLat in -90.0..90.0) { "playerLat out of range" }
        require(playerLng in -180.0..180.0) { "playerLng out of range" }
        require(visitCount >= 0) { "visitCount must not be negative" }
        require(nearbyTreasuresCollected >= 0) { "nearbyTreasuresCollected must not be negative" }
        require(clueStepsCompleted >= 0) { "clueStepsCompleted must not be negative" }
        require(explorerLevel > 0) { "explorerLevel must be positive" }
    }

    fun clueProgressFor(key: String): Int =
        if (activeClueChainKey == key) clueStepsCompleted else 0
}
