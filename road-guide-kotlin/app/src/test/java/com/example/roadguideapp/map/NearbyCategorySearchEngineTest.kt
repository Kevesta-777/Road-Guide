package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class NearbyCategorySearchEngineTest {

    @Test
    fun indexAndRank_deduplicatesAndSortsByDistance() {
        val mapCenter = LatLng(51.5, -0.1)
        val near = PeliasSearchResult("a", "Near", "Near", "venue", 51.501, -0.1)
        val far = PeliasSearchResult("b", "Far", "Far", "venue", 51.55, -0.1)
        val dup = PeliasSearchResult("a", "Near dup", "Near dup", "venue", 51.502, -0.1)

        val ranked = NearbyCategorySearchEngine.indexAndRank(
            searchContext = NearbySearchContext.MapCenter,
            mapCenter = mapCenter,
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
    fun indexAndRank_alongRoute_usesCorridorDistance() {
        val mapCenter = LatLng(51.5, -0.1)
        val route = listOf(
            LatLng(51.50, -0.10),
            LatLng(51.51, -0.10),
        )
        val onRoute = PeliasSearchResult("on", "On route", "On route", "venue", 51.505, -0.10)
        val offRoute = PeliasSearchResult("off", "Off route", "Off route", "venue", 51.505, -0.13)

        val ranked = NearbyCategorySearchEngine.indexAndRank(
            searchContext = NearbySearchContext.AlongRoute(route, radiusMeters = 400.0),
            mapCenter = mapCenter,
            entries = listOf(onRoute to null, offRoute to null),
        )

        assertEquals(1, ranked.size)
        assertEquals("on", ranked[0].result.gid)
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

    @Test
    fun polylineDistance_pointNearSegment() {
        val route = listOf(
            LatLng(51.0, -0.1),
            LatLng(52.0, -0.1),
        )
        val point = LatLng(51.5, -0.1005)
        val distance = PolylineDistance.distanceToPolylineMeters(point, route)
        assertTrue(distance < 200.0)
    }
}
