package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PanoramaHuntRewardsTest {
    @Test
    fun hiddenSymbol_includesAchievementOnly() {
        val bundle = PanoramaHuntRewards.completionBundle(sampleHunt(PanoramaHuntType.HIDDEN_SYMBOL), 1)
        assertNotNull(bundle.achievementKey)
        assertEquals(null, bundle.storyFragmentId)
        assertEquals(null, bundle.legendaryRelicKey)
    }

    @Test
    fun relicHunt_includesAllHookTypes() {
        val bundle = PanoramaHuntRewards.completionBundle(sampleHunt(PanoramaHuntType.RELIC_HUNT), 3)
        assertEquals("story_fragment_relic_hunt", bundle.storyFragmentId)
        assertEquals("panorama_hunt_relic_hunt", bundle.achievementKey)
        assertEquals("legendary_relic_panorama_hunt", bundle.legendaryRelicKey)
    }

    private fun sampleHunt(type: PanoramaHuntType): PanoramaHunt =
        PanoramaHunt.forSecretPlace(
            secretPlaceId = "sp:v1:test:${type.id}:seed1",
            huntType = type,
            panoramaId = "pano-test",
            rewardSeed = 1L,
        )
}
