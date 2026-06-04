package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyCategorySearchTest {

    @Test
    fun mergeResults_deduplicatesByGid() {
        val a = PeliasSearchResult("gid1", "A", "A", "venue", 1.0, 2.0)
        val b = PeliasSearchResult("gid2", "B", "B", "venue", 1.0, 2.0)
        val dup = PeliasSearchResult("gid1", "A duplicate", "A duplicate", "venue", 3.0, 4.0)
        val merged = NearbyCategorySearch.mergeResults(listOf(a, b), listOf(dup, b))
        assertEquals(2, merged.size)
        assertEquals("gid1", merged[0].gid)
        assertEquals("gid2", merged[1].gid)
    }

    @Test
    fun boundsForResults_singleResult_returnsPaddedBounds() {
        val result = PeliasSearchResult("gid1", "Cafe", "Cafe", "venue", 51.5074, -0.1278)
        val bounds = NearbyCategorySearch.boundsForResults(listOf(result))
        assertNotNull(bounds)
        assertTrue(bounds!!.latitudeNorth > bounds.latitudeSouth)
    }

    @Test
    fun boundsForResults_duplicateLocations_returnsPaddedBounds() {
        val a = PeliasSearchResult("gid1", "A", "A", "venue", 51.5074, -0.1278)
        val b = PeliasSearchResult("gid2", "B", "B", "venue", 51.5074, -0.1278)
        val bounds = NearbyCategorySearch.boundsForResults(listOf(a, b))
        assertNotNull(bounds)
        assertTrue(bounds!!.latitudeNorth > bounds.latitudeSouth)
    }
}
