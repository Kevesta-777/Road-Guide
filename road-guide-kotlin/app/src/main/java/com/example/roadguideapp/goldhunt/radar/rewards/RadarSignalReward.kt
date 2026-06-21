package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarSignal
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.radar.RadarType

internal data class RadarSignalReward(
    val signal: RadarSignal,
    val radarType: RadarType,
    val targetRarity: RadarTargetRarity,
    val xpAwarded: Long,
    val creditsAwarded: Int,
    val xpEventId: String,
    val creditEventId: String,
    val achievementKey: String,
) {
    val targetCategory: RadarTargetCategory
        get() = signal.targetCategory
}

internal data class RadarScanRewardBundle(
    val radarType: RadarType,
    val scannedAtMs: Long,
    val signalRewards: List<RadarSignalReward>,
    val pulseXp: Long,
    val pulseCredits: Int,
    val pulseXpEventId: String,
    val pulseCreditEventId: String,
    val totalXp: Long,
    val totalCredits: Int,
    val detectionCount: Int,
) {
    val hasRewards: Boolean
        get() = totalXp > 0L || totalCredits > 0
}
