package com.example.roadguideapp.map

import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TileserverBundledResourcesTest {

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
