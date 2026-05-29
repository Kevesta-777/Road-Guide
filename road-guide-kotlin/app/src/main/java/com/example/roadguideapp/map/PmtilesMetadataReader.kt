package com.example.roadguideapp.map

import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.io.File
import java.io.RandomAccessFile
import java.util.zip.GZIPInputStream

/**
 * Reads TileJSON-like fields from a local PMTiles v3 archive (bounds / center / maxzoom).
 */
internal object PmtilesMetadataReader {

    data class Metadata(
        val fitBounds: LatLngBounds? = null,
        val center: LatLng? = null,
        val maxZoom: Int? = null,
    )

    fun read(file: File): Metadata? {
        if (!file.isFile || file.length() < 128) return null
        return runCatching {
            RandomAccessFile(file, "r").use { raf ->
                val header = ByteArray(64)
                if (raf.read(header) < 64) return@use null
                if (!header.copyOfRange(0, 7).contentEquals("PMTiles".toByteArray())) return@use null
                val metaOffset = readU64(header, 24)
                val metaLength = readU64(header, 32)
                if (metaLength <= 0L || metaLength > 5_000_000L) return@use null
                raf.seek(metaOffset)
                val compressed = ByteArray(metaLength.toInt())
                raf.readFully(compressed)
                val jsonText = decompressMetadata(compressed) ?: return@use null
                parseMetadataJson(jsonText)
            }
        }.getOrNull()
    }

    private fun decompressMetadata(bytes: ByteArray): String? {
        return if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
            GZIPInputStream(bytes.inputStream()).bufferedReader().use { it.readText() }
        } else {
            bytes.toString(Charsets.UTF_8)
        }
    }

    internal fun parseMetadataJson(jsonText: String): Metadata {
        val root = JSONObject(jsonText)
        return Metadata(
            fitBounds = root.optJSONArray("bounds")?.toBounds(),
            center = root.optJSONArray("center")?.toCenter(),
            maxZoom = root.optInt("maxzoom", -1).takeIf { it >= 0 },
        )
    }

    private fun readU64(buffer: ByteArray, offset: Int): Long {
        var value = 0L
        for (i in 0 until 8) {
            value = value or (buffer[offset + i].toLong() and 0xFF shl (8 * i))
        }
        return value
    }

    private fun JSONArray.toBounds(): LatLngBounds? {
        if (length() != 4) return null
        val west = getDouble(0)
        val south = getDouble(1)
        val east = getDouble(2)
        val north = getDouble(3)
        if (!listOf(west, south, east, north).all { it.isFinite() }) return null
        if (west == east || south == north) return null
        return LatLngBounds.Builder()
            .include(LatLng(south, west))
            .include(LatLng(north, east))
            .build()
    }

    private fun JSONArray.toCenter(): LatLng? {
        if (length() < 2) return null
        val lon = getDouble(0)
        val lat = getDouble(1)
        if (!lon.isFinite() || !lat.isFinite()) return null
        return LatLng(lat, lon)
    }
}
