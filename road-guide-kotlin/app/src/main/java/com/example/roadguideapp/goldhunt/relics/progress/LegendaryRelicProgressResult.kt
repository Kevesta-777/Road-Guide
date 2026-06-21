package com.example.roadguideapp.goldhunt.relics.progress

import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRevealResult
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantResult

internal data class LegendaryRelicProgressGrantResult(
    val isNewGrant: Boolean,
    val pieceId: String? = null,
    val relicId: String? = null,
    val relicCompleted: Boolean = false,
    val piecesCollected: Int = 0,
    val pieceCount: Int = 0,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
) {
    companion object {
        val skipped = LegendaryRelicProgressGrantResult(isNewGrant = false)
    }
}

internal data class LegendaryRelicProgressBatchResult(
    val grants: List<LegendaryRelicProgressGrantResult>,
    val progress: LegendaryRelicProgress,
    val rewardGrants: List<LegendaryRelicRewardGrantResult> = emptyList(),
    val hiddenReveals: List<HiddenRelicDiscoveryRevealResult> = emptyList(),
) {
    val newGrantCount: Int get() = grants.count { it.isNewGrant }
    val hasNewGrants: Boolean get() = newGrantCount > 0
    val newRewardGrantCount: Int get() = rewardGrants.count { it.isNewGrant }
    val newHiddenRevealCount: Int get() = hiddenReveals.count { it.isNewReveal }
}
