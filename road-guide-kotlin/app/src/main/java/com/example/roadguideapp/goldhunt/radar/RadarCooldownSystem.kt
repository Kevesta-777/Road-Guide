package com.example.roadguideapp.goldhunt.radar

/**
 * Per-radar-type cooldown rules integrated with [RadarProfile].
 */
internal object RadarCooldownSystem {
    fun cooldownMillis(radarType: RadarType): Long =
        radarType.cooldownSeconds.coerceAtLeast(1).toLong() * 1_000L

    fun remainingCooldownMs(
        state: RadarTypeCooldownState,
        nowMs: Long = System.currentTimeMillis(),
    ): Long {
        val last = state.lastScanTime ?: return 0L
        val elapsed = (nowMs - last).coerceAtLeast(0L)
        return (cooldownMillis(state.radarType) - elapsed).coerceAtLeast(0L)
    }

    fun canScanNow(
        state: RadarTypeCooldownState,
        unlockLevel: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        if (!RadarExplorerLevelGate.isUnlocked(state.radarType, unlockLevel)) return false
        return remainingCooldownMs(state, nowMs) <= 0L
    }

    fun snapshot(
        state: RadarTypeCooldownState,
        unlockLevel: Int,
        nowMs: Long = System.currentTimeMillis(),
    ): RadarCooldownSnapshot {
        val remaining = remainingCooldownMs(state, nowMs)
        return RadarCooldownSnapshot(
            radarType = state.radarType,
            lastScanTime = state.lastScanTime,
            remainingCooldownMs = remaining,
            scanCount = state.scanCount,
            cooldownSeconds = state.radarType.cooldownSeconds,
            canScanNow = RadarExplorerLevelGate.isUnlocked(state.radarType, unlockLevel) &&
                remaining <= 0L,
        )
    }

    fun recordTypeScan(
        state: RadarTypeCooldownState,
        successful: Boolean,
        timestampMs: Long,
    ): RadarTypeCooldownState = state.copy(
        lastScanTime = timestampMs,
        scanCount = state.scanCount + 1,
        successfulScanCount = state.successfulScanCount + if (successful) 1 else 0,
        updatedAtMs = timestampMs,
    )
}
