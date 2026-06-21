package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PanoramaHuntCompletionMapperTest {
    @Test
    fun toProgress_computesFraction() {
        val progress = PanoramaHuntCompletionMapper.toProgress(
            huntId = "hunt-1",
            collectedSlotIndices = setOf(0, 1),
            targetsTotal = 3,
            completedAtMs = null,
        )
        assertEquals(2, progress.targetsFound)
        assertEquals(3, progress.targetsTotal)
        assertEquals(1, progress.remainingCount)
        assertEquals(2f / 3f, progress.progressFraction, 0.001f)
    }

    @Test
    fun completionEntity_roundTripsToDomain() {
        val hunt = PanoramaHunt.forSecretPlace(
            secretPlaceId = "sp:v1:test:HIDDEN_SYMBOL:seed1",
            huntType = PanoramaHuntType.HIDDEN_SYMBOL,
            panoramaId = "pano-1",
            rewardSeed = 9L,
        )
        val entity = PanoramaHuntCompletionMapper.toCompletionEntity(
            hunt = hunt,
            targetsFound = 1,
            targetsTotal = 1,
            creditsGranted = 25,
            xpGranted = 40L,
            completedAtMs = 500L,
        )
        val domain = PanoramaHuntCompletionMapper.toDomain(entity)
        assertNotNull(domain)
        assertEquals(hunt.huntId, domain!!.huntId)
        assertEquals(500L, domain.completedAtMs)
    }

    @Test
    fun targetFindingEntity_preservesIds() {
        val target = PanoramaHiddenTarget(
            targetId = "pht:v1:hunt:s0",
            huntId = "hunt-1",
            slotIndex = 0,
            bearing = 10f,
            pitch = 5f,
            radius = 8f,
            targetType = PanoramaHiddenTargetType.SYMBOL,
        )
        val entity = PanoramaHuntCompletionMapper.toTargetFindingEntity(target, foundAtMs = 100L)
        assertEquals(target.targetId, entity.targetId)
        assertEquals(0, entity.slotIndex)
    }
}
