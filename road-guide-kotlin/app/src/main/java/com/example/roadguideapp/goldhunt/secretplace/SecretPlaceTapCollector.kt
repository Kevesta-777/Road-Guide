package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

internal object SecretPlaceTapCollector {
    suspend fun collectOnTap(
        repository: GoldHuntRepository,
        spec: SecretPlaceSpec,
        mapZoom: Double,
        timestampMs: Long = System.currentTimeMillis(),
    ): SecretPlaceCollectOutcome {
        repository.ensureInitialized()
        if (!SecretPlaceRevealPolicy.canCollectOnTap(spec, repository, mapZoom)) {
            return SecretPlaceCollectOutcome.NotRevealed
        }
        if (repository.isSecretPlaceDiscoveredCached(spec.secretPlaceId)) {
            return SecretPlaceCollectOutcome.AlreadyDiscovered
        }
        val result = repository.discoverSecretPlace(spec, timestampMs)
        if (!result.newlyDiscovered) {
            return SecretPlaceCollectOutcome.AlreadyDiscovered
        }
        return SecretPlaceCollectOutcome.Discovered(
            credits = result.creditsEarned,
            displayName = spec.displayName,
        )
    }
}
