package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

internal data class PanoramaHuntRewardContext(
    val hunt: PanoramaHunt,
    val explorerLevel: Int,
) {
    init {
        require(explorerLevel >= ExplorerLevelCalculator.MIN_LEVEL) {
            "explorerLevel must be at least ${ExplorerLevelCalculator.MIN_LEVEL}"
        }
    }
}
