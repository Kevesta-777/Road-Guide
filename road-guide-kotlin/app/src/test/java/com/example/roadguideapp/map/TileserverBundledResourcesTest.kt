package com.example.roadguideapp.map

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TileserverBundledResourcesTest {

    @Test
    fun assetPathForTileserverHttpUrl_mapsSpriteAndFontPaths() {
        val spriteJson = "http://10.0.2.2:8080/tileserver/sprite/sprites.json".toHttpUrl()
        assertEquals(
            "map/sprites.json",
            TileserverBundledResources.assetPathForTileserverHttpUrl(spriteJson),
        )
        assertTrue(TileserverBundledResources.isTileserverSpriteOrGlyphUrl(spriteJson))

        val glyph = "http://10.0.2.2:8080/tileserver/font/Roboto%20Regular/0-255.pbf".toHttpUrl()
        assertEquals(
            "map/font/Roboto Regular/0-255.pbf",
            TileserverBundledResources.assetPathForTileserverHttpUrl(glyph),
        )

        val tile = "http://10.0.2.2:8080/tileserver/data/v3/14/8192/5461.pbf".toHttpUrl()
        assertNull(TileserverBundledResources.assetPathForTileserverHttpUrl(tile))
        assertFalse(TileserverBundledResources.isTileserverSpriteOrGlyphUrl(tile))
    }

    @Test
    fun isValidSpriteDirectory_rejectsEmptyOrInvalidPng() {
        val root = createTempDir("sprites-invalid").apply {
            deleteOnExit()
        }
        assertFalse(TileserverBundledResources.isValidSpriteDirectory(root))

        File(root, "sprites.json").writeText("""{"dot_10":{"width":1,"height":1,"x":0,"y":0}}""")
        File(root, "sprites.png").writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03))
        assertFalse(TileserverBundledResources.isValidSpriteDirectory(root))
    }

    @Test
    fun isValidSpriteDirectory_acceptsJsonAndPng() {
        val root = createTempDir("sprites-valid").apply {
            deleteOnExit()
        }
        File(root, "sprites.json").writeText("""{"dot_10":{"width":1,"height":1,"x":0,"y":0}}""")
        File(root, "sprites.png").writeBytes(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
        )
        assertTrue(TileserverBundledResources.isValidSpriteDirectory(root))
    }

    @Test
    fun isValidSpriteDirectory_rejectsOneXJsonCopiedToRetinaPair() {
        val root = createTempDir("sprites-retina-mismatch").apply {
            deleteOnExit()
        }
        val spriteJson = """{"dot_10":{"width":1,"height":1,"x":0,"y":0}}"""
        File(root, "sprites.json").writeText(spriteJson)
        File(root, "sprites.png").writeBytes(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
        )
        File(root, "sprites@2x.json").writeText(spriteJson)
        File(root, "sprites@2x.png").writeBytes(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01),
        )
        assertTrue(TileserverBundledResources.isValidSpriteDirectory(root))
        assertFalse(File(root, "sprites@2x.json").exists())
        assertFalse(File(root, "sprites@2x.png").exists())
    }

    @Test
    fun isValidSpriteDirectory_acceptsMatchingRetinaPair() {
        val root = createTempDir("sprites-retina-valid").apply {
            deleteOnExit()
        }
        File(root, "sprites.json").writeText("""{"dot_10":{"width":1,"height":1,"x":0,"y":0}}""")
        File(root, "sprites.png").writeBytes(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
        )
        File(root, "sprites@2x.json").writeText("""{"dot_10":{"width":2,"height":2,"x":0,"y":0}}""")
        File(root, "sprites@2x.png").writeBytes(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01),
        )
        assertTrue(TileserverBundledResources.isValidSpriteDirectory(root))
    }

    @Test
    fun isValidGlyphTree_requiresRobotoStacks() {
        val fontDir = createTempDir("glyphs").apply {
            deleteOnExit()
        }.resolve("font").apply { mkdirs() }
        assertFalse(TileserverBundledResources.isValidGlyphTree(fontDir))

        val regular = fontDir.resolve("Roboto Regular").apply { mkdirs() }
        regular.resolve("0-255.pbf").writeBytes(byteArrayOf(1, 2, 3))
        assertFalse(TileserverBundledResources.isValidGlyphTree(fontDir))

        val medium = fontDir.resolve("Roboto Medium").apply { mkdirs() }
        medium.resolve("0-255.pbf").writeBytes(byteArrayOf(1, 2, 3))
        assertTrue(TileserverBundledResources.isValidGlyphTree(fontDir))
    }
}
