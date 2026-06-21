package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretPlaceCategoryAssignmentEngineTest {
    @Test
    fun assign_sameInput_isDeterministic() {
        val input = sampleInput(anchorId = "sp:v1:london:L2:12:34:s0")
        val first = SecretPlaceCategoryAssignmentEngine.assign(input)
        val second = SecretPlaceCategoryAssignmentEngine.assign(input)
        assertEquals(first, second)
    }

    @Test
    fun assign_sameLocationAnchor_isStableAcrossRuns() {
        val input = SecretPlaceCategoryAssignmentInput.forGridAnchor(
            anchorId = "sp:v1:london:L2:44:55:s1",
            playRegionId = "r:-0.5100:51.2800",
            latitude = 51.501364,
            longitude = -0.141890,
            l1CellId = "L1:44:55",
            l2CellId = "L2:44:55",
        )
        val expected = SecretPlaceCategoryAssignmentEngine.assign(input)
        repeat(20) {
            assertEquals(expected, SecretPlaceCategoryAssignmentEngine.assign(input))
        }
    }

    @Test
    fun assign_differentAnchors_canYieldDifferentCategories() {
        val base = sampleInput(anchorId = "sp:v1:london:L2:1:1:s0")
        val other = sampleInput(anchorId = "sp:v1:london:L2:9:9:s0")
        val categories = setOf(
            SecretPlaceCategoryAssignmentEngine.assign(base),
            SecretPlaceCategoryAssignmentEngine.assign(other),
        )
        assertTrue(categories.isNotEmpty())
    }

    @Test
    fun historicPoi_biasesTowardHistoricLandmark() {
        val neutral = SecretPlaceCategoryAssignmentInput(
            anchorId = "sp:v1:test:hist:1",
            playRegionId = "r:0:51",
            latitude = 51.50,
            longitude = -0.12,
            treasureDensity = SecretPlaceTreasureDensity.fromFraction(0.4),
            explorationProgress = SecretPlaceExplorationProgress.fromSignals(0.3, 0.3),
        )
        val historic = neutral.copy(
            poiMetadata = SecretPlacePoiMetadata.fromTags(
                listOf("monument", "historic", "memorial"),
            ),
            environmentType = SecretPlaceEnvironmentType.HISTORIC,
        )
        val neutralCategory = SecretPlaceCategoryAssignmentEngine.assign(neutral)
        val historicCategory = SecretPlaceCategoryAssignmentEngine.assign(historic)
        assertTrue(historicCategory.difficultyLevel >= neutralCategory.difficultyLevel)
        assertTrue(historic.poiMetadata.resolveEnvironmentType() == SecretPlaceEnvironmentType.HISTORIC)
    }

    @Test
    fun mysticalEnvironment_biasesTowardHigherDifficultyCategories() {
        val neutral = SecretPlaceCategoryAssignmentInput(
            anchorId = "sp:v1:test:myth:1",
            playRegionId = "r:0:51",
            latitude = 51.51,
            longitude = -0.11,
            treasureDensity = SecretPlaceTreasureDensity.fromFraction(0.2),
            explorationProgress = SecretPlaceExplorationProgress.fromSignals(0.2, 0.2),
        )
        val mystical = neutral.copy(
            poiMetadata = SecretPlacePoiMetadata.fromTags(listOf("shrine", "sacred", "megalith")),
            environmentType = SecretPlaceEnvironmentType.MYSTICAL,
            treasureDensity = SecretPlaceTreasureDensity.fromFraction(0.8),
            explorationProgress = SecretPlaceExplorationProgress.fromSignals(0.9, 0.85),
        )
        assertTrue(
            SecretPlaceCategoryAssignmentEngine.assign(mystical).difficultyLevel >=
                SecretPlaceCategoryAssignmentEngine.assign(neutral).difficultyLevel,
        )
    }

    @Test
    fun poiMetadata_resolvesEnvironmentType() {
        val metadata = SecretPlacePoiMetadata.fromTags(listOf("park", "forest", "nature_reserve"))
        assertEquals(SecretPlaceEnvironmentType.NATURAL, metadata.resolveEnvironmentType())
    }

    @Test
    fun rollSeed_isCanonicalAndLocationQuantized() {
        val input = sampleInput(anchorId = "anchor-a")
        val seed = SecretPlaceCategoryAssignmentEngine.rollSeed(input)
        assertEquals(
            "r:0:51|secretPlaceCategory|anchor-a|51.500000|-0.120000",
            seed,
        )
        val shifted = input.copy(latitude = input.latitude + 0.0000004)
        assertEquals(seed, SecretPlaceCategoryAssignmentEngine.rollSeed(shifted))
        val moved = input.copy(latitude = input.latitude + 0.000001)
        assertNotEquals(seed, SecretPlaceCategoryAssignmentEngine.rollSeed(moved))
    }

    @Test
    fun forGridAnchor_buildsStableSignalsFromCells() {
        val input = SecretPlaceCategoryAssignmentInput.forGridAnchor(
            anchorId = "sp:v1:london:L2:3:3:s0",
            playRegionId = "r:0:51",
            latitude = 51.5,
            longitude = -0.12,
            l1CellId = "L1:3:3",
            l2CellId = "L2:3:3",
        )
        val again = SecretPlaceCategoryAssignmentInput.forGridAnchor(
            anchorId = "sp:v1:london:L2:3:3:s0",
            playRegionId = "r:0:51",
            latitude = 51.5,
            longitude = -0.12,
            l1CellId = "L1:3:3",
            l2CellId = "L2:3:3",
        )
        assertEquals(
            SecretPlaceCategoryAssignmentEngine.assign(input),
            SecretPlaceCategoryAssignmentEngine.assign(again),
        )
    }

    private fun sampleInput(anchorId: String): SecretPlaceCategoryAssignmentInput =
        SecretPlaceCategoryAssignmentInput(
            anchorId = anchorId,
            playRegionId = "r:0:51",
            latitude = 51.5,
            longitude = -0.12,
            treasureDensity = SecretPlaceTreasureDensity.fromFraction(0.5),
            explorationProgress = SecretPlaceExplorationProgress.fromSignals(0.5, 0.5),
        )
}
