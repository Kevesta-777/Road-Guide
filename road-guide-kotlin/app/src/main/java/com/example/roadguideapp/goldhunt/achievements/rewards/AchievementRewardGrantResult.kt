package com.example.roadguideapp.goldhunt.achievements.rewards

internal data class AchievementRewardGrantResult(
    val achievementId: String,
    val creditsGranted: Int,
    val xpGranted: Long,
    val storyFragmentRecorded: Boolean,
    val legendaryRelicRecorded: Boolean,
    val titleRecorded: Boolean,
    val badgeRecorded: Boolean,
    val isNewGrant: Boolean,
) {
    companion object {
        val skipped = AchievementRewardGrantResult(
            achievementId = "",
            creditsGranted = 0,
            xpGranted = 0L,
            storyFragmentRecorded = false,
            legendaryRelicRecorded = false,
            titleRecorded = false,
            badgeRecorded = false,
            isNewGrant = false,
        )
    }
}
