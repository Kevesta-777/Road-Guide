package com.example.roadguideapp.map

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Offline-first cache keyed by tileserver origin. One "visited location" corresponds to one
 * [HEADWAY_TILESERVER_BASE_URL]; revisiting reuse stored map metadata and resolved style JSON
 * instead of fetching from the tileserver again.
 */
internal object TileserverLocationDiskCache {

    private const val ROOT_DIR = "map_tileserver_location_cache"
    private const val AREAMAP_FILE = "areamap.json"
    /** Bump when resolved-style URL rewriting changes (invalidates on-disk cache). */
    private const val RESOLVED_STYLE_CACHE_VERSION = "v6"
    private const val RESOLVED_STYLE_PREFIX = "style_${RESOLVED_STYLE_CACHE_VERSION}_"
    private const val RESOLVED_STYLE_SUFFIX = "_resolved.json"

    private fun normalizedOrigin(origin: String): String = origin.trimEnd('/')

    fun originDirectory(context: Context, tileserverOrigin: String): File {
        val safe = slugForOrigin(normalizedOrigin(tileserverOrigin))
        return File(context.applicationContext.filesDir, ROOT_DIR).resolve(safe).also { it.mkdirs() }
    }

    fun readAreamapJson(context: Context, tileserverOrigin: String): String? {
        val file = originDirectory(context, tileserverOrigin).resolve(AREAMAP_FILE)
        if (!file.isFile) return null
        return runCatching { file.readText(Charsets.UTF_8) }.getOrNull()
    }

    fun writeAreamapJson(context: Context, tileserverOrigin: String, json: String) {
        writeAtomically(originDirectory(context, tileserverOrigin).resolve(AREAMAP_FILE), json)
    }

    fun readResolvedStyleJson(
        context: Context,
        tileserverOrigin: String,
        stylePath: String = AppMapStyle.TILESERVER_STYLE_PATH,
    ): String? {
        val file = originDirectory(context, tileserverOrigin).resolve(resolvedStyleFileName(stylePath))
        if (!file.isFile) return null
        return runCatching { file.readText(Charsets.UTF_8) }.getOrNull()
    }

    fun writeResolvedStyleJson(
        context: Context,
        tileserverOrigin: String,
        stylePath: String,
        json: String,
    ) {
        writeAtomically(
            originDirectory(context, tileserverOrigin).resolve(resolvedStyleFileName(stylePath)),
            json,
        )
    }

    private fun resolvedStyleFileName(stylePath: String): String {
        val slug = slugForOrigin(stylePath.trim().ifBlank { AppMapStyle.TILESERVER_STYLE_PATH })
        return RESOLVED_STYLE_PREFIX + slug.take(16) + RESOLVED_STYLE_SUFFIX
    }

    private fun slugForOrigin(origin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(origin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { b -> "%02x".format(b) }
    }

    private fun writeAtomically(target: File, content: String) {
        val parent = target.parentFile ?: return
        if (!parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${target.name}.tmp")
        tmp.outputStream().bufferedWriter(Charsets.UTF_8).use { it.write(content) }
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
    }
}
