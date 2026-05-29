package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class NearbyCategorySearchEngineTest {

    @Test
    fun indexAndRank_deduplicatesAndSortsByDistance() {
        val center = LatLng(51.5, -0.1)
        val near = PeliasSearchResult("a", "Near", "Near", "venue", 51.501, -0.1)
        val far = PeliasSearchResult("b", "Far", "Far", "venue", 51.55, -0.1)
        val dup = PeliasSearchResult("a", "Near dup", "Near dup", "venue", 51.502, -0.1)

        val ranked = NearbyCategorySearchEngine.indexAndRank(
            center = center,
            entries = listOf(
                far to null,
                dup to null,
                near to null,
            ),
        )

        assertEquals(2, ranked.size)
        assertEquals("a", ranked[0].result.gid)
        assertEquals("b", ranked[1].result.gid)
        assert(ranked[0].distanceMeters < ranked[1].distanceMeters)
    }

    @Test
    fun findNearestResult_returnsClosestWithinThreshold() {
        val results = listOf(
            PeliasSearchResult("1", "A", "A", null, 51.500, -0.100),
            PeliasSearchResult("2", "B", "B", null, 51.510, -0.100),
        )
        val tap = LatLng(51.5005, -0.1005)
        val nearest = NearbyCategorySearchEngine.findNearestResult(results, tap, maxDistanceMeters = 200.0)
        assertNotNull(nearest)
        assertEquals("1", nearest?.gid)
    }
}
