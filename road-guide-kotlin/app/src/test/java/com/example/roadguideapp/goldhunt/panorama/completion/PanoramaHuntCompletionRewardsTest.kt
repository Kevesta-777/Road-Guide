package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntCompletionRewardsTest {
    @Test
    fun rewards_scaleWithDifficulty() {
        val easy = hunt(PanoramaHuntType.HIDDEN_SYMBOL)
        val hard = hunt(PanoramaHuntType.RELIC_HUNT)
        assertTrue(PanoramaHuntCompletionRewards.creditsFor(hard) > PanoramaHuntCompletionRewards.creditsFor(easy))
        assertTrue(PanoramaHuntCompletionRewards.xpFor(hard) > PanoramaHuntCompletionRewards.xpFor(easy))
    }

    @Test
    fun rewards_scaleWithExplorerLevel() {
        val hunt = hunt(PanoramaHuntType.PANORAMA_PUZZLE)
        assertTrue(PanoramaHuntCompletionRewards.creditsFor(hunt, 8) > PanoramaHuntCompletionRewards.creditsFor(hunt, 1))
    }

    @Test
    fun rewardDispatcher_usesStableEventId() {
        val hunt = hunt(PanoramaHuntType.OBJECT_HUNT)
        val grant = PanoramaHuntCompletionRewardDispatcher.grantFor(hunt, explorerLevel = 2)
        assertTrue(grant.eventId.contains(hunt.huntId))
        assertTrue(grant.amount > 0)
    }

    private fun hunt(type: PanoramaHuntType): PanoramaHunt = PanoramaHunt.forSecretPlace(
        secretPlaceId = "sp:v1:test:${type.id}:seed1",
        huntType = type,
        panoramaId = "pano-test",
        rewardSeed = 1L,
    )
}
