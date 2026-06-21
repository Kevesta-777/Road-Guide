package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

internal object RadarProfileMapper {
    fun toDomain(entity: RadarProfileEntity): RadarProfile {
        val radarType = RadarType.fromId(entity.currentRadarTypeId) ?: RadarType.BASIC_RADAR
        return RadarProfile(
            currentRadarType = radarType,
            unlockLevel = entity.unlockLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL),
            lastScanTime = entity.lastScanTimeMs.takeIf { it > RadarSchema.NEVER_SCANNED_MS },
            totalScans = entity.totalScans.coerceAtLeast(0),
            successfulScans = entity.successfulScans.coerceAtLeast(0),
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
            createdAtMs = entity.createdAtMs,
            updatedAtMs = entity.updatedAtMs,
        )
    }

    fun toEntity(
        profile: RadarProfile,
        id: Int = RadarSchema.SINGLETON_ID,
    ): RadarProfileEntity = RadarProfileEntity(
        id = id,
        currentRadarTypeId = profile.currentRadarType.id,
        unlockLevel = profile.unlockLevel.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL),
        lastScanTimeMs = profile.lastScanTime ?: RadarSchema.NEVER_SCANNED_MS,
        totalScans = profile.totalScans.coerceAtLeast(0),
        successfulScans = profile.successfulScans.coerceAtLeast(0).coerceAtMost(
            profile.totalScans.coerceAtLeast(0),
        ),
        schemaVersion = profile.schemaVersion,
        extensionJson = profile.extensionJson,
        createdAtMs = profile.createdAtMs,
        updatedAtMs = profile.updatedAtMs,
    )
}
