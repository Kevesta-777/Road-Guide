package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarSignal
import com.example.roadguideapp.goldhunt.radar.RadarType
import kotlin.math.roundToLong

/**
 * Pure reward math for radar pulses.
 */
internal object RadarRewardCalculator {
    fun calculateForScan(scanResult: RadarScanResult): RadarScanRewardBundle {
        val signalRewards = scanResult.signals.map { calculateForSignal(it, scanResult.radarType) }
        val pulseXp = RadarRewardConfig.pulseCompletionXp(
            scanResult.radarType,
            scanResult.hasDetections,
        )
        val pulseCredits = RadarRewardConfig.pulseCompletionCredits(
            scanResult.radarType,
            scanResult.hasDetections,
        )
        val signalXp = signalRewards.sumOf { it.xpAwarded }
        val signalCredits = signalRewards.sumOf { it.creditsAwarded }
        return RadarScanRewardBundle(
            radarType = scanResult.radarType,
            scannedAtMs = scanResult.scannedAtMs,
            signalRewards = signalRewards,
            pulseXp = pulseXp,
            pulseCredits = pulseCredits,
            pulseXpEventId = pulseXpEventId(scanResult.radarType, scanResult.scannedAtMs),
            pulseCreditEventId = pulseCreditEventId(scanResult.radarType, scanResult.scannedAtMs),
            totalXp = signalXp + pulseXp,
            totalCredits = signalCredits + pulseCredits,
            detectionCount = signalRewards.size,
        )
    }

    fun calculateForSignal(
        signal: RadarSignal,
        radarType: RadarType,
    ): RadarSignalReward {
        val rarity = RadarTargetRarityResolver.resolve(signal)
        val strength = RadarRewardConfig.strengthFactor(signal.signalStrength)
        val typeFactor = RadarRewardConfig.radarTypeFactor(radarType)
        val xp = (
            RadarRewardConfig.baseXp(radarType) *
                strength *
                rarity.xpMultiplier *
                typeFactor
            ).roundToLong().coerceAtLeast(1L)
        val credits = (
            RadarRewardConfig.baseCredits(radarType) *
                strength *
                rarity.creditMultiplier *
                typeFactor
            ).roundToLong().toInt().coerceAtLeast(1)
        return RadarSignalReward(
            signal = signal,
            radarType = radarType,
            targetRarity = rarity,
            xpAwarded = xp,
            creditsAwarded = credits,
            xpEventId = detectionXpEventId(signal),
            creditEventId = detectionCreditEventId(signal),
            achievementKey = RadarRewardSchema.AchievementKeys.detectionReward(
                radarType,
                signal.targetCategory,
            ),
        )
    }

    fun detectionXpEventId(signal: RadarSignal): String =
        "${RadarRewardSchema.EventPrefixes.XP}:${signal.detectionEventId()}"

    fun detectionCreditEventId(signal: RadarSignal): String =
        "${RadarRewardSchema.EventPrefixes.CREDIT}:${signal.detectionEventId()}"

    fun pulseXpEventId(radarType: RadarType, scannedAtMs: Long): String =
        "${RadarRewardSchema.EventPrefixes.PULSE_XP}:${radarType.id.lowercase()}:$scannedAtMs"

    fun pulseCreditEventId(radarType: RadarType, scannedAtMs: Long): String =
        "${RadarRewardSchema.EventPrefixes.PULSE_CREDIT}:${radarType.id.lowercase()}:$scannedAtMs"
}
