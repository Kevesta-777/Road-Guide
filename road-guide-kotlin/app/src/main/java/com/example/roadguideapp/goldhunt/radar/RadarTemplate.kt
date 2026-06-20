package com.example.roadguideapp.goldhunt.radar

/**
 * Read-only catalog template for one [RadarType] and its reserved hook keys.
 */
internal data class RadarTemplate(
    val type: RadarType,
    val displayName: String,
    val unlockLevel: Int,
    val detectionRangeMeters: Int,
    val cooldownSeconds: Int,
    val achievementKey: String?,
    val seasonalEventKey: String?,
    val legendaryRelicKey: String?,
) {
    companion object {
        fun from(type: RadarType): RadarTemplate = RadarTemplate(
            type = type,
            displayName = type.displayName,
            unlockLevel = type.unlockLevel,
            detectionRangeMeters = type.detectionRangeMeters,
            cooldownSeconds = type.cooldownSeconds,
            achievementKey = RadarSchema.AchievementKeys.firstUnlock(type),
            seasonalEventKey = RadarSchema.SeasonalEventKeys.forType(type),
            legendaryRelicKey = RadarSchema.LegendaryRelicKeys.forType(type),
        )
    }
}
