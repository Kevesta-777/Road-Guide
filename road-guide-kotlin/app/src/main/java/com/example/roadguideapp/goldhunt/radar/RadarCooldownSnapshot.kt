package com.example.roadguideapp.goldhunt.radar

/**
 * Runtime cooldown view for one equipped or queried radar type.
 */
internal data class RadarCooldownSnapshot(
    val radarType: RadarType,
    val lastScanTime: Long?,
    val remainingCooldownMs: Long,
    val scanCount: Int,
    val cooldownSeconds: Int,
    val canScanNow: Boolean,
) {
    val cooldownMillis: Long
        get() = cooldownSeconds.coerceAtLeast(1).toLong() * 1_000L

    val remainingCooldownSeconds: Int
        get() = ((remainingCooldownMs + 999L) / 1_000L).toInt().coerceAtLeast(0)
}
