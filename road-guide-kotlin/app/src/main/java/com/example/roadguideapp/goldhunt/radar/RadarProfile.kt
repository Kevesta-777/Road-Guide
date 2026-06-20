package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Persisted explorer radar state (offline, single-row in `goldhunt.db`).
 *
 * [unlockLevel] mirrors the explorer level used for radar unlock checks and reconciliation.
 * [lastScanTime] mirrors the equipped radar's last pulse from [typeCooldownStates].
 */
internal data class RadarProfile(
    val currentRadarType: RadarType,
    val unlockLevel: Int,
    val lastScanTime: Long?,
    val totalScans: Int,
    val successfulScans: Int,
    val typeCooldownStates: Map<RadarType, RadarTypeCooldownState> = RadarTypeCooldownState.defaults(),
    val schemaVersion: Int = RadarSchema.VERSION,
    val extensionJson: String = RadarSchema.EMPTY_EXTENSIONS_JSON,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
) {
    val successRate: Double
        get() = if (totalScans <= 0) 0.0 else successfulScans.toDouble() / totalScans.toDouble()

    val isCurrentRadarUnlocked: Boolean
        get() = RadarExplorerLevelGate.isUnlocked(currentRadarType, unlockLevel)

    fun detectionProfile(): RadarDetectionProfile =
        RadarDetectionProfile.from(currentRadarType)

    fun typeCooldownState(type: RadarType = currentRadarType): RadarTypeCooldownState =
        typeCooldownStates[type] ?: RadarTypeCooldownState.default(type)

    fun cooldownRemainingMs(
        nowMs: Long = System.currentTimeMillis(),
        radarType: RadarType = currentRadarType,
    ): Long = RadarCooldownSystem.remainingCooldownMs(typeCooldownState(radarType), nowMs)

    fun cooldownSnapshot(
        nowMs: Long = System.currentTimeMillis(),
        radarType: RadarType = currentRadarType,
    ): RadarCooldownSnapshot = RadarCooldownSystem.snapshot(
        state = typeCooldownState(radarType),
        unlockLevel = unlockLevel,
        nowMs = nowMs,
    )

    fun canScanNow(
        nowMs: Long = System.currentTimeMillis(),
        radarType: RadarType = currentRadarType,
    ): Boolean = RadarCooldownSystem.canScanNow(
        state = typeCooldownState(radarType),
        unlockLevel = unlockLevel,
        nowMs = nowMs,
    )

    fun withTypeCooldownStates(
        states: Map<RadarType, RadarTypeCooldownState>,
    ): RadarProfile {
        val currentState = states[currentRadarType] ?: typeCooldownState()
        return copy(
            typeCooldownStates = states,
            lastScanTime = currentState.lastScanTime,
        )
    }

    companion object {
        fun default(nowMs: Long = System.currentTimeMillis()): RadarProfile = RadarProfile(
            currentRadarType = RadarType.BASIC_RADAR,
            unlockLevel = RadarSchema.DEFAULT_UNLOCK_LEVEL.coerceAtLeast(
                ExplorerLevelCalculator.MIN_LEVEL,
            ),
            lastScanTime = null,
            totalScans = 0,
            successfulScans = 0,
            typeCooldownStates = RadarTypeCooldownState.defaults(nowMs),
            schemaVersion = RadarSchema.VERSION,
            extensionJson = RadarSchema.EMPTY_EXTENSIONS_JSON,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        )
    }
}
