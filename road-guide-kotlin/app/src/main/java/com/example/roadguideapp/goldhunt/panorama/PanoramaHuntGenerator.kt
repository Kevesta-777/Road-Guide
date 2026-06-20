package com.example.roadguideapp.goldhunt.panorama

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Deterministic procedural generator for [PanoramaHunt] instances.
 *
 * Rolls use [TreasureHash] (SHA-256) — never [kotlin.random.Random] or [java.util.Random].
 * Identical inputs produce identical hunts after app or device restart (fully offline).
 *
 * Does not persist completion state, render panorama UI, or dispatch rewards.
 */
internal object PanoramaHuntGenerator {
    fun generate(input: PanoramaHuntGenerationInput): PanoramaHunt {
        val baseSeed = seedKey(input)
        val huntType = PanoramaHuntTypePicker.pick(baseSeed)
        val rewardSeed = rewardSeedLong(baseSeed)
        return PanoramaHunt.forSecretPlace(
            secretPlaceId = input.secretPlaceId,
            huntType = huntType,
            panoramaId = input.panoramaId,
            rewardSeed = rewardSeed,
        )
    }

    fun generate(
        panoramaId: String,
        secretPlaceId: String,
        worldSeed: String,
    ): PanoramaHunt = generate(
        PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        ),
    )

    /** Exposed for tests — canonical seed string passed to [TreasureHash]. */
    fun seedKey(input: PanoramaHuntGenerationInput): String =
        listOf(
            input.worldSeed,
            "panoramaHunt",
            input.panoramaId,
            input.secretPlaceId,
            "v${PanoramaHuntGenerationConfig.GENERATOR_VERSION}",
        ).joinToString("|")

    private fun rewardSeedLong(baseSeed: String): Long {
        val hi = TreasureHash.digest32("$baseSeed:seed:hi").toLong() and 0xFFFFFFFFL
        val lo = TreasureHash.digest32("$baseSeed:seed:lo").toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }
}
