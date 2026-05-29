package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PmtilesOverviewStylePatchTest {

    @Test
    fun pmtilesFileUrl_usesFileProtocolWithAbsolutePath() {
        val file = File("e:/tmp/GreaterLondon.pmtiles")
        assertTrue(PmtilesOverviewSource.pmtilesFileUrl(file).startsWith("pmtiles://file://"))
        assertTrue(PmtilesOverviewSource.pmtilesFileUrl(file).endsWith("GreaterLondon.pmtiles"))
    }

    @Test
    fun overviewLayerId_prefixesBaseId() {
        assertEquals("overview_boundary", PmtilesOverviewStylePatch.overviewLayerId("boundary"))
        assertTrue(PmtilesOverviewStylePatch.isOverviewLayerId("overview_water"))
        assertFalse(PmtilesOverviewStylePatch.isOverviewLayerId("water"))
    }

    @Test
    fun overviewSourceLayers_includeBoundariesAndRoads() {
        assertTrue("boundary" in PmtilesOverviewStylePatch.OVERVIEW_SOURCE_LAYERS)
        assertTrue("transportation" in PmtilesOverviewStylePatch.OVERVIEW_SOURCE_LAYERS)
        assertFalse("building" in PmtilesOverviewStylePatch.OVERVIEW_SOURCE_LAYERS)
    }

}
