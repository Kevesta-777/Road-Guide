package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadarProfileMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesFields() {
        val domain = RadarProfile(
            currentRadarType = RadarType.TREASURE_RADAR,
            unlockLevel = 5,
            lastScanTime = 1_700_000_000_000L,
            totalScans = 12,
            successfulScans = 7,
            schemaVersion = RadarSchema.VERSION,
            extensionJson = RadarSchema.EMPTY_EXTENSIONS_JSON,
            createdAtMs = 100L,
            updatedAtMs = 200L,
        )
        val entity = RadarProfileMapper.toEntity(domain)
        val roundTrip = RadarProfileMapper.toDomain(entity)

        assertEquals(domain.currentRadarType, roundTrip.currentRadarType)
        assertEquals(domain.unlockLevel, roundTrip.unlockLevel)
        assertEquals(domain.lastScanTime, roundTrip.lastScanTime)
        assertEquals(domain.totalScans, roundTrip.totalScans)
        assertEquals(domain.successfulScans, roundTrip.successfulScans)
        assertEquals(domain.createdAtMs, roundTrip.createdAtMs)
        assertEquals(domain.updatedAtMs, roundTrip.updatedAtMs)
    }

    @Test
    fun neverScanned_mapsToNullInDomain() {
        val entity = RadarProfileEntity(lastScanTimeMs = RadarSchema.NEVER_SCANNED_MS)
        val domain = RadarProfileMapper.toDomain(entity)
        assertNull(domain.lastScanTime)
        assertEquals(RadarSchema.NEVER_SCANNED_MS, RadarProfileMapper.toEntity(domain).lastScanTimeMs)
    }

    @Test
    fun unknownRadarType_fallsBackToBasic() {
        val entity = RadarProfileEntity(currentRadarTypeId = "UNKNOWN_RADAR")
        val domain = RadarProfileMapper.toDomain(entity)
        assertEquals(RadarType.BASIC_RADAR, domain.currentRadarType)
    }
}
