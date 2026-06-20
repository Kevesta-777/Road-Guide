package com.example.roadguideapp.goldhunt.repository

internal data class SecretPlaceDiscoveryResult(
    val creditsEarned: Int,
    val newlyDiscovered: Boolean,
    val overlayDirty: Boolean,
    val xpEarned: Long = 0L,
) {
    companion object {
        fun skipped() = SecretPlaceDiscoveryResult(
            creditsEarned = 0,
            newlyDiscovered = false,
            overlayDirty = false,
            xpEarned = 0L,
        )
    }
}
