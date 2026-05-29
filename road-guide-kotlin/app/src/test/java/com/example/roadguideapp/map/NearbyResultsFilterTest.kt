package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class NearbyResultsFilterTest {

    private fun result(gid: String, name: String) = PeliasSearchResult(
        gid = gid,
        label = "$name, City",
        name = name,
        layer = "venue",
        latitude = 35.0,
        longitude = 139.0,
    )

    @Test
    fun priceTierIndex_isStableForSameGid() {
        val r = result("stable-gid", "Shop")
        assertEquals(
            NearbyResultsFilter.priceTierIndex(r),
            NearbyResultsFilter.priceTierIndex(r),
        )
    }

    @Test
    fun brandKey_usesSegmentBeforeDash() {
        assertEquals(
            "Shake Shack",
            NearbyResultsFilter.brandKey(result("x", "Shake Shack Tokyo")),
        )
        assertEquals(
            "McDonald's Ichigaya-Shop",
            NearbyResultsFilter.brandKey(result("y", "McDonald's Ichigaya-Shop")),
        )
        assertEquals(
            "McDonald's Ichigaya",
            NearbyResultsFilter.brandKey(result("z", "McDonald's Ichigaya - Shop")),
        )
    }

    @Test
    fun apply_chainFilter_limitsToBrand() {
        val results = listOf(
            result("1", "Alpha One"),
            result("2", "Beta Two"),
        )
        val filtered = NearbyResultsFilter.apply(
            results = results,
            picks = emptyList(),
            filter = NearbyResultsFilter.State(selectedChain = "Alpha One"),
        )
        assertEquals(1, filtered.visibleResults.size)
        assertTrue(filtered.visibleResults.first().name.startsWith("Alpha"))
    }
}
