package com.example.roadguideapp.goldhunt.radar

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressRepository
import com.example.roadguideapp.goldhunt.radar.rewards.RadarRewardGrantManager
import com.example.roadguideapp.goldhunt.radar.rewards.RadarRewardGrantOutcome
import com.example.roadguideapp.goldhunt.radar.rewards.RadarRewardStatistics
import com.example.roadguideapp.goldhunt.radar.direction.RadarDirectionalScanResult
import com.example.roadguideapp.goldhunt.radar.statistics.RadarStatistics
import com.example.roadguideapp.goldhunt.radar.statistics.RadarStatisticsManager

/**
 * Orchestrates persisted radar profile updates, per-type cooldown state, and achievement hooks.
 */
internal class RadarProfileManager private constructor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val repository = RadarProfileRepository.get(context)
    private val cooldownRepository = RadarTypeCooldownStateRepository.get(context)
    private val achievementRepository = PanoramaHuntAchievementProgressRepository.get(context)
    private val rewardGrantManager = RadarRewardGrantManager.get(context)
    private val statisticsManager = RadarStatisticsManager.get(context)

    suspend fun loadProfile(): RadarProfile = loadProfileWithCooldown()

    suspend fun ensureProfile(timestampMs: Long = System.currentTimeMillis()): RadarProfile {
        repository.ensureProfile(timestampMs)
        cooldownRepository.ensureAllTypes(timestampMs)
        return loadProfileWithCooldown()
    }

    suspend fun loadProfileWithCooldown(
        nowMs: Long = System.currentTimeMillis(),
    ): RadarProfile {
        val base = repository.loadProfile()
        val states = cooldownRepository.ensureAllTypes(nowMs)
        return base.withTypeCooldownStates(states)
    }

    suspend fun cooldownSnapshot(
        radarType: RadarType? = null,
        nowMs: Long = System.currentTimeMillis(),
    ): RadarCooldownSnapshot {
        val profile = loadProfileWithCooldown(nowMs)
        return profile.cooldownSnapshot(nowMs, radarType ?: profile.currentRadarType)
    }

    suspend fun reconcileWithExplorerLevel(
        explorerLevel: Int,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarProfile = updateProfileWithCooldown(timestampMs) { profile ->
        RadarProfileReconciler.reconcileWithExplorerLevel(profile, explorerLevel)
    }

    suspend fun equipRadar(
        radarType: RadarType,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarProfileEquipResult {
        var result: RadarProfileEquipResult = RadarProfileEquipResult.Blocked(
            profile = RadarProfile.default(timestampMs),
            levelsUntilUnlocked = 0,
        )
        updateProfileWithCooldown(timestampMs) { profile ->
            result = RadarProfileReconciler.equipRadar(profile, radarType)
            val equipped = result.profile
            equipped.copy(
                lastScanTime = equipped.typeCooldownState(radarType).lastScanTime,
            )
        }
        return result
    }

    suspend fun recordScan(
        successful: Boolean,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarProfileRecordScanResult {
        var result: RadarProfileRecordScanResult = RadarProfileRecordScanResult.Blocked(
            profile = RadarProfile.default(timestampMs),
        )
        updateProfileWithCooldown(timestampMs) { profile ->
            result = RadarProfileReconciler.recordScan(profile, successful, timestampMs)
            result.profile
        }
        val recorded = result as? RadarProfileRecordScanResult.Recorded
        if (recorded != null) {
            grantScanAchievements(recorded, timestampMs)
        }
        return result
    }

    suspend fun performScan(
        latitude: Double,
        longitude: Double,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarScanWithCooldownResult {
        val profile = loadProfileWithCooldown(timestampMs)
        if (!profile.canScanNow(timestampMs)) {
            return RadarScanWithCooldownResult.CooldownActive(
                profile = profile,
                cooldown = profile.cooldownSnapshot(timestampMs),
            )
        }
        val scanResult = RadarScanEngine.scan(
            context = appContext,
            latitude = latitude,
            longitude = longitude,
            scannedAtMs = timestampMs,
        )
        val recordResult = recordScan(
            successful = scanResult.hasDetections,
            timestampMs = timestampMs,
        )
        val rewardOutcome = when (recordResult) {
            is RadarProfileRecordScanResult.Recorded ->
                rewardGrantManager.grantForScan(scanResult)
            else -> null
        }
        val statisticsOutcome = when (recordResult) {
            is RadarProfileRecordScanResult.Recorded ->
                statisticsManager.recordScan(scanResult, timestampMs)
            else -> null
        }
        return when (recordResult) {
            is RadarProfileRecordScanResult.Recorded -> RadarScanWithCooldownResult.Completed(
                profile = recordResult.profile,
                scanResult = scanResult,
                cooldown = recordResult.profile.cooldownSnapshot(timestampMs),
                rewardOutcome = rewardOutcome,
                statistics = statisticsOutcome,
            )
            is RadarProfileRecordScanResult.CooldownActive -> RadarScanWithCooldownResult.CooldownActive(
                profile = recordResult.profile,
                cooldown = recordResult.profile.cooldownSnapshot(timestampMs),
            )
            is RadarProfileRecordScanResult.Blocked -> RadarScanWithCooldownResult.Blocked(
                profile = recordResult.profile,
            )
        }
    }

    private suspend fun updateProfileWithCooldown(
        timestampMs: Long,
        transform: (RadarProfile) -> RadarProfile,
    ): RadarProfile {
        val current = loadProfileWithCooldown(timestampMs)
        val next = transform(current).copy(
            createdAtMs = current.createdAtMs.takeIf { it > 0L } ?: timestampMs,
            updatedAtMs = timestampMs,
        )
        repository.saveProfile(next, timestampMs)
        cooldownRepository.saveAll(next.typeCooldownStates.values)
        return next
    }

    suspend fun loadRewardStatistics(): RadarRewardStatistics =
        rewardGrantManager.loadStatistics()

    suspend fun loadStatistics(): RadarStatistics = statisticsManager.load()

    private suspend fun grantScanAchievements(
        result: RadarProfileRecordScanResult.Recorded,
        timestampMs: Long,
    ) {
        val achievementKey = RadarSchema.AchievementKeys.firstUnlock(result.radarType)
        if (result.isFirstScanForType) {
            achievementRepository.increment(achievementKey, timestampMs = timestampMs)
        }
        if (result.isFirstSuccessfulScanForType) {
            achievementRepository.increment(
                achievementKey = "$achievementKey:success",
                timestampMs = timestampMs,
            )
        }
        val unlockedCount = RadarExplorerLevelGate.unlockedTypes(result.profile.unlockLevel).size
        if (unlockedCount >= RadarType.ALL_ORDERED.size) {
            achievementRepository.increment(
                achievementKey = RadarSchema.AchievementKeys.unlockAll(),
                timestampMs = timestampMs,
            )
        }
    }

    companion object {
        @Volatile
        private var instance: RadarProfileManager? = null

        fun get(context: Context): RadarProfileManager =
            instance ?: synchronized(this) {
                instance ?: RadarProfileManager(context.applicationContext).also { instance = it }
            }
    }
}

internal sealed class RadarScanWithCooldownResult {
    data class Completed(
        val profile: RadarProfile,
        val scanResult: RadarScanResult,
        val cooldown: RadarCooldownSnapshot,
        val rewardOutcome: RadarRewardGrantOutcome?,
        val statistics: RadarStatistics?,
    ) : RadarScanWithCooldownResult() {
        val directionalScan: RadarDirectionalScanResult
            get() = scanResult.toDirectionalView()
    }

    data class CooldownActive(
        val profile: RadarProfile,
        val cooldown: RadarCooldownSnapshot,
    ) : RadarScanWithCooldownResult()

    data class Blocked(
        val profile: RadarProfile,
    ) : RadarScanWithCooldownResult()
}
