package com.example.roadguideapp.goldhunt.radar

/**
 * Persisted cooldown and scan counters for one [RadarType].
 */
internal data class RadarTypeCooldownState(
    val radarType: RadarType,
    val lastScanTime: Long?,
    val scanCount: Int,
    val successfulScanCount: Int,
    val updatedAtMs: Long = 0L,
) {
    companion object {
        fun default(
            radarType: RadarType,
            nowMs: Long = System.currentTimeMillis(),
        ): RadarTypeCooldownState = RadarTypeCooldownState(
            radarType = radarType,
            lastScanTime = null,
            scanCount = 0,
            successfulScanCount = 0,
            updatedAtMs = nowMs,
        )

        fun defaults(nowMs: Long = System.currentTimeMillis()): Map<RadarType, RadarTypeCooldownState> =
            RadarType.ALL_ORDERED.associateWith { default(it, nowMs) }
    }
}
