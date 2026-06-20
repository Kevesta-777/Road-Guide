package com.example.roadguideapp.goldhunt.panorama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntGeneratorTest {
    private val worldSeed = PanoramaHuntWorldSeed.DEFAULT
    private val secretPlaceId = "sp:v1:r:-0.5000:51.4000:L2:12:34:s0"
    private val panoramaId = "pano-abc123"

    @Test
    fun generate_sameInputs_producesIdenticalHunt() {
        val input = PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        )
        val first = PanoramaHuntGenerator.generate(input)
        val second = PanoramaHuntGenerator.generate(input)
        assertEquals(first, second)
    }

    @Test
    fun generate_samePanorama_isDeterministicAcrossCalls() {
        val huntA = PanoramaHuntGenerator.generate(panoramaId, secretPlaceId, worldSeed)
        val huntB = PanoramaHuntGenerator.generate(panoramaId, secretPlaceId, worldSeed)
        assertEquals(huntA.huntId, huntB.huntId)
        assertEquals(huntA.huntType, huntB.huntType)
        assertEquals(huntA.rewardSeed, huntB.rewardSeed)
        assertEquals(huntA.difficulty, huntB.difficulty)
    }

    @Test
    fun generate_differentPanoramaId_producesDifferentHunt() {
        val base = PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        )
        val other = base.copy(panoramaId = "pano-other")
        val a = PanoramaHuntGenerator.generate(base)
        val b = PanoramaHuntGenerator.generate(other)
        assertNotEquals(a, b)
        assertNotEquals(a.huntId, b.huntId)
    }

    @Test
    fun generate_differentSecretPlaceId_producesDifferentHunt() {
        val base = PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        )
        val other = base.copy(secretPlaceId = "sp:v1:other:place:seed9")
        val a = PanoramaHuntGenerator.generate(base)
        val b = PanoramaHuntGenerator.generate(other)
        assertNotEquals(a, b)
    }

    @Test
    fun generate_differentWorldSeed_producesDifferentHunt() {
        val base = PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        )
        val other = base.copy(worldSeed = "${worldSeed}:alt")
        val a = PanoramaHuntGenerator.generate(base)
        val b = PanoramaHuntGenerator.generate(other)
        assertNotEquals(a, b)
    }

    @Test
    fun generate_supportsAllFiveHuntTypes() {
        val seen = mutableSetOf<PanoramaHuntType>()
        for (index in 0 until 256) {
            val hunt = PanoramaHuntGenerator.generate(
                panoramaId = "pano-scan-$index",
                secretPlaceId = secretPlaceId,
                worldSeed = worldSeed,
            )
            seen += hunt.huntType
            if (seen.size == PanoramaHuntType.entries.size) break
        }
        assertEquals(PanoramaHuntType.entries.toSet(), seen)
    }

    @Test
    fun seedKey_isStableAcrossCalls() {
        val input = PanoramaHuntGenerationInput(
            panoramaId = panoramaId,
            secretPlaceId = secretPlaceId,
            worldSeed = worldSeed,
        )
        assertEquals(
            PanoramaHuntGenerator.seedKey(input),
            PanoramaHuntGenerator.seedKey(input),
        )
    }

    @Test
    fun generate_buildsStableHuntIdFromInputs() {
        val hunt = PanoramaHuntGenerator.generate(panoramaId, secretPlaceId, worldSeed)
        assertEquals(
            PanoramaHuntId.forSecretPlace(
                secretPlaceId = secretPlaceId,
                huntType = hunt.huntType,
                panoramaId = panoramaId,
                rewardSeed = hunt.rewardSeed,
            ),
            hunt.huntId,
        )
        assertEquals(panoramaId, hunt.panoramaId)
        assertEquals(secretPlaceId, hunt.secretPlaceId)
        assertTrue(hunt.difficulty > 0)
        assertEquals(hunt.huntType.difficulty, hunt.difficulty)
    }
}
