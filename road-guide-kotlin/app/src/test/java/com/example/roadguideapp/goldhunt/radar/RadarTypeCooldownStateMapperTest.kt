package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadarTypeCooldownStateMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesFields() {
        val domain = RadarTypeCooldownState(
            radarType = RadarType.STORY_RADAR,
            lastScanTime = 1_700_000_000_000L,
            scanCount = 7,
            successfulScanCount = 4,
            updatedAtMs = 1_700_000_100_000L,
        )
        val roundTrip = RadarTypeCooldownStateMapper.toDomain(
            RadarTypeCooldownStateMapper.toEntity(domain),
        )
        assertEquals(domain, roundTrip)
    }

    @Test
    fun neverScanned_mapsToNullInDomain() {
        val entity = RadarTypeCooldownStateEntity(
            radarTypeId = RadarType.BASIC_RADAR.id,
            lastScanTimeMs = RadarSchema.NEVER_SCANNED_MS,
        )
        assertNull(RadarTypeCooldownStateMapper.toDomain(entity).lastScanTime)
    }
}
