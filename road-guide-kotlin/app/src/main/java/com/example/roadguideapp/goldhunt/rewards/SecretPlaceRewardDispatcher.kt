package com.example.roadguideapp.goldhunt.rewards

import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec

internal object SecretPlaceRewardDispatcher {
    /** Secret places are message-only discoveries; no credits are granted. */
    fun grantsForDiscovery(spec: SecretPlaceSpec): List<CreditGrant> = emptyList()
}
