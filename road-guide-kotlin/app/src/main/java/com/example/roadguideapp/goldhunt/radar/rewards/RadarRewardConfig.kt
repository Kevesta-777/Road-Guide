package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarType

/** Offline tuning for radar pulse rewards. */
internal object RadarRewardConfig {
    fun baseXp(radarType: RadarType): Long = when (radarType) {
        RadarType.BASIC_RADAR -> 2L
        RadarType.TREASURE_RADAR -> 4L
        RadarType.SECRET_PLACE_RADAR -> 6L
        RadarType.CLUSTER_RADAR -> 8L
        RadarType.STORY_RADAR -> 12L
        RadarType.LEGENDARY_RADAR -> 20L
    }

    fun baseCredits(radarType: RadarType): Int = when (radarType) {
        RadarType.BASIC_RADAR -> 1
        RadarType.TREASURE_RADAR -> 2
        RadarType.SECRET_PLACE_RADAR -> 3
        RadarType.CLUSTER_RADAR -> 4
        RadarType.STORY_RADAR -> 6
        RadarType.LEGENDARY_RADAR -> 10
    }

    fun pulseCompletionXp(radarType: RadarType, hasDetections: Boolean): Long {
        val base = when (radarType) {
            RadarType.BASIC_RADAR -> 3L
            RadarType.TREASURE_RADAR -> 5L
            RadarType.SECRET_PLACE_RADAR -> 8L
            RadarType.CLUSTER_RADAR -> 10L
            RadarType.STORY_RADAR -> 15L
            RadarType.LEGENDARY_RADAR -> 25L
        }
        return if (hasDetections) base else base / 2
    }

    fun pulseCompletionCredits(radarType: RadarType, hasDetections: Boolean): Int {
        val base = when (radarType) {
            RadarType.BASIC_RADAR -> 2
            RadarType.TREASURE_RADAR -> 3
            RadarType.SECRET_PLACE_RADAR -> 5
            RadarType.CLUSTER_RADAR -> 6
            RadarType.STORY_RADAR -> 8
            RadarType.LEGENDARY_RADAR -> 12
        }
        return if (hasDetections) base else base / 2
    }

    /** Maps signal strength 0..1 to a 50%..100% reward factor. */
    fun strengthFactor(signalStrength: Double): Double =
        (0.5 + signalStrength.coerceIn(0.0, 1.0) * 0.5).coerceIn(0.5, 1.0)

    fun radarTypeFactor(radarType: RadarType): Double = when (radarType) {
        RadarType.BASIC_RADAR -> 1.0
        RadarType.TREASURE_RADAR -> 1.1
        RadarType.SECRET_PLACE_RADAR -> 1.2
        RadarType.CLUSTER_RADAR -> 1.35
        RadarType.STORY_RADAR -> 1.5
        RadarType.LEGENDARY_RADAR -> 2.0
    }
}
