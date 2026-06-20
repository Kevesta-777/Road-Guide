package com.example.roadguideapp.goldhunt.radar

/**
 * Schema metadata for radar systems.
 * Bump [VERSION] when adding persistence columns; use [EMPTY_EXTENSIONS_JSON] for experiments first.
 */
internal object RadarSchema {
    const val VERSION = 3
    const val COOLDOWN_SCHEMA_VERSION = 1
    const val SINGLETON_ID = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"
    const val NEVER_SCANNED_MS = 0L
    const val DEFAULT_UNLOCK_LEVEL = 1
    const val DEFAULT_RADAR_TYPE_ID = "BASIC_RADAR"

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val SEASONAL_EVENT = "seasonalEvent"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val UNLOCKED_AT_MS = "unlockedAtMs"
        const val LAST_PULSE_AT_MS = "lastPulseAtMs"
        const val DETECTION_RANGE_M = "detectionRangeM"
        const val COOLDOWN_SECONDS = "cooldownSeconds"
    }

    /** Reserved JSON keys for [RadarProfileEntity.extensionJson]. */
    object ProfileExtensionKeys {
        const val UPGRADES = "upgrades"
        const val ACHIEVEMENT_GRANTS = "achievementGrants"
        const val EQUIPPED_AT_MS = "equippedAtMs"
        const val UNLOCKED_TYPE_IDS = "unlockedTypeIds"
    }

    object UpgradeKeys {
        const val RANGE_BONUS_M = "rangeBonusM"
        const val COOLDOWN_REDUCTION_PCT = "cooldownReductionPct"
        const val SCAN_SUCCESS_BONUS_PCT = "scanSuccessBonusPct"
    }

    object AchievementKeys {
        const val PREFIX = "radar_type"

        fun firstUnlock(type: RadarType): String =
            "$PREFIX:${type.id.lowercase()}:first_pulse"

        fun unlockAll(): String = "$PREFIX:all_radars_unlocked"
    }

    object SeasonalEventKeys {
        const val PREFIX = "seasonal_event_radar"

        fun forType(type: RadarType): String? = when (type) {
            RadarType.LEGENDARY_RADAR -> "$PREFIX:legendary_scope"
            RadarType.STORY_RADAR -> "$PREFIX:story_chronicle"
            else -> null
        }
    }

    object LegendaryRelicKeys {
        const val PREFIX = "legendary_relic_radar"

        fun forType(type: RadarType): String? = when (type) {
            RadarType.LEGENDARY_RADAR -> "$PREFIX:scope"
            RadarType.STORY_RADAR -> "$PREFIX:chronicle_lens"
            else -> null
        }
    }

    object EventPrefixes {
        const val UNLOCK = "radar:unlock"
        const val PULSE = "radar:pulse"
        const val DETECTION = "radar:detection"
    }

    object SignalExtensionKeys {
        const val BEARING_DEG = "bearingDeg"
        const val COMPASS_DIRECTION_ID = "compassDirectionId"
        const val SUBTYPE_ID = "subtypeId"
        const val RADAR_TYPE_ID = "radarTypeId"
    }

    object TargetIdPrefixes {
        const val STORY_FRAGMENT = "story_fragment_"
        const val LEGENDARY_RELIC = "legendary_relic_"
    }

    const val MIN_SIGNAL_STRENGTH = 0.0
    const val MAX_SIGNAL_STRENGTH = 1.0

    fun unlockEventId(type: RadarType): String =
        "${EventPrefixes.UNLOCK}:${type.id.lowercase()}"

    fun pulseEventId(type: RadarType, timestampMs: Long): String =
        "${EventPrefixes.PULSE}:${type.id.lowercase()}:$timestampMs"

    fun detectionEventId(
        category: RadarTargetCategory,
        targetId: String,
        timestampMs: Long,
    ): String =
        "${EventPrefixes.DETECTION}:${category.id.lowercase()}:$targetId:$timestampMs"
}
