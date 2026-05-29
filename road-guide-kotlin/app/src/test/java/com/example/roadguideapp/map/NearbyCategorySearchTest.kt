package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
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
    fun mergeResults_respectsLimit() {
        val primary = (1..10).map {
            PeliasSearchResult("p$it", "P$it", "P$it", null, 0.0, 0.0)
        }
        val merged = NearbyCategorySearch.mergeResults(primary, emptyList(), limit = 5)
        assertEquals(5, merged.size)
    }
}
