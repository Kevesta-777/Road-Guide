package com.example.roadguideapp.goldhunt.radar.rewards

/**
 * Persisted aggregate radar reward statistics.
 */
internal data class RadarRewardStatistics(
    val totalXpEarned: Long = 0L,
    val totalCreditsEarned: Int = 0,
    val rewardedScans: Int = 0,
    val rewardedDetections: Int = 0,
    val treasureDetectionsRewarded: Int = 0,
    val clusterDetectionsRewarded: Int = 0,
    val secretPlaceDetectionsRewarded: Int = 0,
    val storyFragmentDetectionsRewarded: Int = 0,
    val legendaryRelicDetectionsRewarded: Int = 0,
    val schemaVersion: Int = RadarRewardSchema.VERSION,
    val updatedAtMs: Long = 0L,
) {
    companion object {
        fun empty(nowMs: Long = System.currentTimeMillis()): RadarRewardStatistics =
            RadarRewardStatistics(updatedAtMs = nowMs)
    }
}
