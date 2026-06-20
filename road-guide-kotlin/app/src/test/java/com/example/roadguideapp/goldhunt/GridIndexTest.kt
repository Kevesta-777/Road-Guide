package com.example.roadguideapp.goldhunt

import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GridIndexTest {
    private val region = PlayRegion(
        west = -0.55,
        south = 51.28,
        east = 0.35,
        north = 51.69,
        totalCellsL0 = 1L,
    )

    @Test
    fun encode_returnsStableCellId() {
        val cell = GridIndex.encode(51.5074, -0.1278, level = 0, region = region)
        assertNotNull(cell)
        assertEquals("L0", cell!!.id.substring(0, 2))
    }

    @Test
    fun parentL1_alignsToCoarseGrid() {
        val cell = GridIndex.encode(51.5074, -0.1278, level = 0, region = region)!!
        val parent = GridIndex.parentL1(cell, region)
        assertNotNull(parent)
        assertEquals(1, parent!!.level)
    }
}
