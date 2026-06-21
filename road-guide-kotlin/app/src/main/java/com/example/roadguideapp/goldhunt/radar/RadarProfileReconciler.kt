package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Pure reconciliation rules between explorer level and persisted radar state.
 */
internal object RadarProfileReconciler {
    fun reconcileUnlockLevel(
        profile: RadarProfile,
        explorerLevel: Int,
    ): RadarProfile {
        val level = explorerLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
        if (profile.unlockLevel == level) return profile
        return profile.copy(unlockLevel = level)
    }

    fun clampCurrentRadar(profile: RadarProfile): RadarProfile {
        if (RadarExplorerLevelGate.isUnlocked(profile.currentRadarType, profile.unlockLevel)) {
            return profile
        }
        val fallback = RadarCatalog.highestUnlockedAt(profile.unlockLevel)?.type
            ?: RadarType.BASIC_RADAR
        if (fallback == profile.currentRadarType) return profile
        return profile.copy(currentRadarType = fallback)
    }

    fun reconcileWithExplorerLevel(
        profile: RadarProfile,
        explorerLevel: Int,
    ): RadarProfile = clampCurrentRadar(
        reconcileUnlockLevel(profile, explorerLevel),
    )

    fun equipRadar(
        profile: RadarProfile,
        radarType: RadarType,
    ): RadarProfileEquipResult {
        if (!RadarExplorerLevelGate.isUnlocked(radarType, profile.unlockLevel)) {
            return RadarProfileEquipResult.Blocked(
                profile = profile,
                levelsUntilUnlocked = RadarExplorerLevelGate.levelsUntilUnlocked(
                    radarType,
                    profile.unlockLevel,
                ),
            )
        }
        if (profile.currentRadarType == radarType) {
            return RadarProfileEquipResult.AlreadyEquipped(profile)
        }
        val typeState = profile.typeCooldownState(radarType)
        return RadarProfileEquipResult.Equipped(
            profile.copy(
                currentRadarType = radarType,
                lastScanTime = typeState.lastScanTime,
            ),
        )
    }

    fun recordScan(
        profile: RadarProfile,
        successful: Boolean,
        timestampMs: Long,
    ): RadarProfileRecordScanResult {
        val radarType = profile.currentRadarType
        val currentState = profile.typeCooldownState(radarType)
        if (!profile.isCurrentRadarUnlocked) {
            return RadarProfileRecordScanResult.Blocked(profile)
        }
        val remaining = RadarCooldownSystem.remainingCooldownMs(currentState, timestampMs)
        if (remaining > 0L) {
            return RadarProfileRecordScanResult.CooldownActive(
                profile = profile,
                cooldownRemainingMs = remaining,
                radarType = radarType,
            )
        }
        val updatedTypeState = RadarCooldownSystem.recordTypeScan(
            state = currentState,
            successful = successful,
            timestampMs = timestampMs,
        )
        val updatedStates = profile.typeCooldownStates + (radarType to updatedTypeState)
        val nextTotal = profile.totalScans + 1
        val nextSuccessful = profile.successfulScans + if (successful) 1 else 0
        val updated = profile.copy(
            lastScanTime = timestampMs,
            totalScans = nextTotal,
            successfulScans = nextSuccessful,
            typeCooldownStates = updatedStates,
        )
        return RadarProfileRecordScanResult.Recorded(
            profile = updated,
            radarType = radarType,
            isFirstScanForType = currentState.scanCount == 0,
            isFirstSuccessfulScanForType = successful && currentState.successfulScanCount == 0,
            isFirstScan = profile.totalScans == 0,
            isFirstSuccessfulScan = successful && profile.successfulScans == 0,
        )
    }
}

internal sealed class RadarProfileEquipResult {
    abstract val profile: RadarProfile

    data class Equipped(override val profile: RadarProfile) : RadarProfileEquipResult()
    data class AlreadyEquipped(override val profile: RadarProfile) : RadarProfileEquipResult()
    data class Blocked(
        override val profile: RadarProfile,
        val levelsUntilUnlocked: Int,
    ) : RadarProfileEquipResult()
}

internal sealed class RadarProfileRecordScanResult {
    abstract val profile: RadarProfile

    data class Recorded(
        override val profile: RadarProfile,
        val radarType: RadarType,
        val isFirstScanForType: Boolean,
        val isFirstSuccessfulScanForType: Boolean,
        val isFirstScan: Boolean,
        val isFirstSuccessfulScan: Boolean,
    ) : RadarProfileRecordScanResult()

    data class CooldownActive(
        override val profile: RadarProfile,
        val cooldownRemainingMs: Long,
        val radarType: RadarType,
    ) : RadarProfileRecordScanResult()

    data class Blocked(override val profile: RadarProfile) : RadarProfileRecordScanResult()
}
