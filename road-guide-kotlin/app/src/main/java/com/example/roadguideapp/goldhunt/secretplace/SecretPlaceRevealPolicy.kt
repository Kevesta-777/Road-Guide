package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

internal object SecretPlaceRevealPolicy {
    suspend fun isVisible(
        spec: SecretPlaceSpec,
        repository: GoldHuntRepository,
    ): Boolean {
        if (!repository.areSecretPlacesUnlocked()) return false
        return repository.isInPlayRegion(spec.lat, spec.lng)
    }

    suspend fun canDiscover(
        repository: GoldHuntRepository,
        mapZoom: Double,
    ): Boolean =
        repository.areSecretPlacesUnlocked() &&
            mapZoom >= GoldHuntConfig.SECRET_COLLECT_MIN_ZOOM

    /** Visible on the map and close enough zoom to collect via tap. */
    suspend fun canCollectOnTap(
        spec: SecretPlaceSpec,
        repository: GoldHuntRepository,
        mapZoom: Double,
    ): Boolean = isVisible(spec, repository) && canDiscover(repository, mapZoom)
}
