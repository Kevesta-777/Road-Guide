package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Test

class TileserverStyleLoaderTest {

    @Test
    fun rewriteStyleUrl_keepsTileserverPrefixForHeadway() {
        val origin = MapServerConfig.tileserverBaseUrl
        assertEquals(
            "$origin/tileserver/data/default.json",
            TileserverStyleLoader.rewriteStyleUrl("/tileserver/data/default.json", origin),
        )
        assertEquals(
            "$origin/tileserver/sprite/sprites",
            TileserverStyleLoader.rewriteStyleUrl("/tileserver/sprite/sprites", origin),
        )
        assertEquals(
            "$origin/tileserver/font/roboto/0-255",
            TileserverStyleLoader.rewriteStyleUrl("/tileserver/font/roboto/0-255", origin),
        )
    }

    @Test
    fun rewriteStyleUrl_leavesNonPathsUnchanged() {
        assertEquals(
            "https://example.com/x",
            TileserverStyleLoader.rewriteStyleUrl("https://example.com/x", "http://h:8000"),
        )
    }
}
