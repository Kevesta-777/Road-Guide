package com.example.roadguideapp.goldhunt.relics.rewards

internal data class LegendaryRelicRewardGrantResult(
    val relicId: String,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
    val storyFragmentRecorded: Boolean = false,
    val titleRecorded: Boolean = false,
    val badgeRecorded: Boolean = false,
    val cosmeticRecorded: Boolean = false,
    val isNewGrant: Boolean = false,
) {
    companion object {
        val skipped = LegendaryRelicRewardGrantResult(
            relicId = "",
            isNewGrant = false,
        )
    }
}
