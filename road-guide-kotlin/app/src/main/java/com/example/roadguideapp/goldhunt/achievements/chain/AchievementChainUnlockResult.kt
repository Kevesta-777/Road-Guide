package com.example.roadguideapp.goldhunt.achievements.chain

internal data class AchievementChainUnlockResult(
    val sourceAchievementId: String,
    val achievementUnlocksRecorded: Int = 0,
    val badgeUnlocksRecorded: Int = 0,
    val titleUnlocksRecorded: Int = 0,
    val storyFragmentsRecorded: Int = 0,
    val legendaryRelicsRecorded: Int = 0,
) {
    val totalRecorded: Int =
        achievementUnlocksRecorded +
            badgeUnlocksRecorded +
            titleUnlocksRecorded +
            storyFragmentsRecorded +
            legendaryRelicsRecorded

    val isNew: Boolean get() = totalRecorded > 0
}
