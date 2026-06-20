package com.example.roadguideapp.goldhunt.secretplace

internal sealed class SecretPlaceCollectOutcome {
    data class Discovered(
        val credits: Int,
        val displayName: String,
    ) : SecretPlaceCollectOutcome()

    data object AlreadyDiscovered : SecretPlaceCollectOutcome()

    data object NotRevealed : SecretPlaceCollectOutcome()

    data object NotReady : SecretPlaceCollectOutcome()
}
