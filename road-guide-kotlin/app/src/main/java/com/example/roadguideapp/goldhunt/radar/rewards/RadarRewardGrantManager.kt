package com.example.roadguideapp.goldhunt.radar.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressRepository
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.profile.xp.XpAwardOutcome
import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

/**
 * Grants explorer XP, credits, achievement progress, and statistics for radar pulses.
 */
internal class RadarRewardGrantManager private constructor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val gameRepository = GoldHuntRepository.get(appContext)
    private val xpAwarder = GameplayXpAwarder.get(appContext)
    private val achievementRepository = PanoramaHuntAchievementProgressRepository.get(appContext)
    private val statisticsRepository = RadarRewardStatisticsRepository.get(appContext)

    suspend fun grantForScan(scanResult: RadarScanResult): RadarRewardGrantOutcome {
        val bundle = RadarRewardCalculator.calculateForScan(scanResult)
        val grantsById = RadarRewardDispatcher.grantsFor(bundle).associateBy { it.eventId }
        var grantedXp = 0L
        var grantedCredits = 0
        var newlyGrantedDetections = 0
        val newlyGrantedSignalRewards = ArrayList<RadarSignalReward>(bundle.signalRewards.size)

        for (reward in bundle.signalRewards) {
            val xpOutcome = xpAwarder.tryAwardRadarDetection(
                eventId = reward.xpEventId,
                xpAwarded = reward.xpAwarded,
                radarType = reward.radarType,
                targetCategory = reward.targetCategory,
                targetId = reward.signal.targetId,
                targetRarity = reward.targetRarity,
                signalStrength = reward.signal.signalStrength,
                timestampMs = scanResult.scannedAtMs,
            )
            val xpGranted = when (xpOutcome) {
                is XpAwardOutcome.Awarded -> {
                    grantedXp += xpOutcome.xpAwarded
                    true
                }
                else -> false
            }
            val creditGrant = grantsById[reward.creditEventId]
            val creditAmount = if (creditGrant != null) {
                gameRepository.applyCreditGrant(creditGrant, scanResult.scannedAtMs)
            } else {
                0
            }
            if (creditAmount > 0) {
                grantedCredits += creditAmount
            }
            if (xpGranted || creditAmount > 0) {
                newlyGrantedDetections++
                newlyGrantedSignalRewards += reward
                achievementRepository.increment(
                    achievementKey = reward.achievementKey,
                    timestampMs = scanResult.scannedAtMs,
                )
            }
        }

        if (bundle.pulseXp > 0L) {
            val pulseXpOutcome = xpAwarder.tryAwardRadarPulse(
                eventId = bundle.pulseXpEventId,
                xpAwarded = bundle.pulseXp,
                radarType = bundle.radarType,
                hasDetections = scanResult.hasDetections,
                timestampMs = scanResult.scannedAtMs,
            )
            if (pulseXpOutcome is XpAwardOutcome.Awarded) {
                grantedXp += pulseXpOutcome.xpAwarded
            }
        }

        val pulseGrant = grantsById[bundle.pulseCreditEventId]
        if (pulseGrant != null) {
            grantedCredits += gameRepository.applyCreditGrant(
                grant = pulseGrant,
                timestampMs = scanResult.scannedAtMs,
            )
        }

        if (grantedXp > 0L || grantedCredits > 0) {
            achievementRepository.increment(
                achievementKey = RadarRewardSchema.AchievementKeys.scanRewardEarned(bundle.radarType),
                timestampMs = scanResult.scannedAtMs,
            )
        }

        val currentStats = statisticsRepository.load()
        val nextStats = RadarRewardStatisticsMapper.applyBundle(
            current = currentStats,
            grantedSignalRewards = newlyGrantedSignalRewards,
            grantedXp = grantedXp,
            grantedCredits = grantedCredits,
            scanRecorded = grantedXp > 0L || grantedCredits > 0,
            timestampMs = scanResult.scannedAtMs,
        )
        val savedStats = statisticsRepository.save(nextStats, scanResult.scannedAtMs)

        return RadarRewardGrantOutcome(
            bundle = bundle,
            grantedXp = grantedXp,
            grantedCredits = grantedCredits,
            statistics = savedStats,
            newlyGrantedDetectionCount = newlyGrantedDetections,
        )
    }

    suspend fun loadStatistics(): RadarRewardStatistics = statisticsRepository.load()

    companion object {
        @Volatile
        private var instance: RadarRewardGrantManager? = null

        fun get(context: Context): RadarRewardGrantManager =
            instance ?: synchronized(this) {
                instance ?: RadarRewardGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
