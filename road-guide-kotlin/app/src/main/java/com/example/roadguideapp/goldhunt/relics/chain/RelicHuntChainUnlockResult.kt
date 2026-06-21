package com.example.roadguideapp.goldhunt.relics.chain

internal data class RelicHuntChainUnlockResult(
    val sourceRelicId: String,
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
