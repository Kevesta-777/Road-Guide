package com.example.roadguideapp.goldhunt.radar.rewards

internal data class RadarRewardGrantOutcome(
    val bundle: RadarScanRewardBundle,
    val grantedXp: Long,
    val grantedCredits: Int,
    val statistics: RadarRewardStatistics,
    val newlyGrantedDetectionCount: Int,
) {
    val hasNewGrants: Boolean
        get() = grantedXp > 0L || grantedCredits > 0
}
