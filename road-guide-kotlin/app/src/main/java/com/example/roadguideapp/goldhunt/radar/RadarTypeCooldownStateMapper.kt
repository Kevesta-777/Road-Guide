package com.example.roadguideapp.goldhunt.radar

internal object RadarTypeCooldownStateMapper {
    fun toDomain(entity: RadarTypeCooldownStateEntity): RadarTypeCooldownState {
        val radarType = RadarType.fromId(entity.radarTypeId) ?: RadarType.BASIC_RADAR
        return RadarTypeCooldownState(
            radarType = radarType,
            lastScanTime = entity.lastScanTimeMs.takeIf { it > RadarSchema.NEVER_SCANNED_MS },
            scanCount = entity.scanCount.coerceAtLeast(0),
            successfulScanCount = entity.successfulScanCount.coerceAtLeast(0),
            updatedAtMs = entity.updatedAtMs,
        )
    }

    fun toEntity(state: RadarTypeCooldownState): RadarTypeCooldownStateEntity =
        RadarTypeCooldownStateEntity(
            radarTypeId = state.radarType.id,
            lastScanTimeMs = state.lastScanTime ?: RadarSchema.NEVER_SCANNED_MS,
            scanCount = state.scanCount.coerceAtLeast(0),
            successfulScanCount = state.successfulScanCount.coerceAtLeast(0).coerceAtMost(
                state.scanCount.coerceAtLeast(0),
            ),
            schemaVersion = RadarSchema.COOLDOWN_SCHEMA_VERSION,
            updatedAtMs = state.updatedAtMs,
        )
}
