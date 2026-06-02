package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Test

class PeliasSearchClientMergeTest {

    @Test
    fun mergeRoundRobin_interleavesAcrossBuckets() {
        fun r(gid: String) = PeliasSearchResult(gid, gid, gid, "venue", 0.0, 0.0)
        val merged = PeliasSearchClient.mergeRoundRobin(
            buckets = listOf(
                listOf(r("a1"), r("a2"), r("a3")),
                listOf(r("b1"), r("b2")),
                listOf(r("c1")),
            ),
            limit = 5,
        )
        assertEquals(listOf("a1", "b1", "c1", "a2", "b2"), merged.map { it.gid })
    }
}
