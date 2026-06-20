package com.example.roadguideapp.goldhunt.radar

/**
 * Runtime tuning snapshot for one equipped radar pulse.
 * Does not track cooldown state or render UI.
 */
internal data class RadarDetectionProfile(
    val radarType: RadarType,
    val displayName: String,
    val detectionRangeMeters: Int,
    val cooldownSeconds: Int,
    val achievementKey: String?,
    val seasonalEventKey: String?,
    val legendaryRelicKey: String?,
    val schemaVersion: Int = RadarSchema.VERSION,
) {
    val cooldownMillis: Long
        get() = cooldownSeconds.coerceAtLeast(1).toLong() * 1_000L

    companion object {
        fun from(type: RadarType): RadarDetectionProfile {
            val template = RadarCatalog.templateFor(type)
            return RadarDetectionProfile(
                radarType = type,
                displayName = template.displayName,
                detectionRangeMeters = template.detectionRangeMeters,
                cooldownSeconds = template.cooldownSeconds,
                achievementKey = template.achievementKey,
                seasonalEventKey = template.seasonalEventKey,
                legendaryRelicKey = template.legendaryRelicKey,
            )
        }

        fun fromTemplate(template: RadarTemplate): RadarDetectionProfile =
            RadarDetectionProfile(
                radarType = template.type,
                displayName = template.displayName,
                detectionRangeMeters = template.detectionRangeMeters,
                cooldownSeconds = template.cooldownSeconds,
                achievementKey = template.achievementKey,
                seasonalEventKey = template.seasonalEventKey,
                legendaryRelicKey = template.legendaryRelicKey,
            )
    }
}
