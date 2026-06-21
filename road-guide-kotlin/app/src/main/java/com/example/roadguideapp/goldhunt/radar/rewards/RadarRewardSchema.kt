package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.radar.RadarType

internal object RadarRewardSchema {
    const val VERSION = 1
    const val STATISTICS_SINGLETON_ID = 1

    object EventPrefixes {
        const val XP = "radar:reward:xp"
        const val CREDIT = "radar:reward:credit"
        const val PULSE_XP = "radar:reward:pulse:xp"
        const val PULSE_CREDIT = "radar:reward:pulse:credit"
    }

    object AchievementKeys {
        const val PREFIX = "radar_reward"

        fun detectionReward(radarType: RadarType, category: RadarTargetCategory): String =
            "$PREFIX:${radarType.id.lowercase()}:${category.id.lowercase()}"

        fun scanRewardEarned(radarType: RadarType): String =
            "$PREFIX:${radarType.id.lowercase()}:scan_earned"
    }
}
