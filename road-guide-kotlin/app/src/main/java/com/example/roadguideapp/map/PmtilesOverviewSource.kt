package com.example.roadguideapp.map

import android.content.Context
import android.util.Log
import com.example.roadguideapp.BuildConfig
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Resolves a local PMTiles URL for MapLibre.
 *
 * MapLibre reads PMTiles via HTTP-style range requests. On Android, [pmtiles://asset://]
 * is unreliable (see maplibre-native #3304) and can crash native code with "invalid header" /
 * decompression errors. Copy the bundled archive to [Context.filesDir] and use
 * [pmtiles://file://] instead.
 *
 * To ship another region later, change [BuildConfig.OVERVIEW_PMTILES_ASSET_PATH] (and the
 * build-time copy task). The on-device cache file name follows the asset file name.
 */
internal object PmtilesOverviewSource {

    private const val TAG = "PmtilesOverviewSource"
    private const val FILES_SUBDIR = "map"

    private val copyLock = Any()
    private val prepareStarted = AtomicBoolean(false)

    @Volatile
    private var prepareLatch = CountDownLatch(1)

    /**
     * Returns a MapLibre-ready `pmtiles://file://…` URL, or null when overview is disabled or
     * the asset cannot be prepared.
     */
    fun prepareAsync(context: Context) {
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) return
        if (!prepareStarted.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        Thread {
            runCatching { resolveUrl(appContext) }
                .onFailure { e -> Log.e(TAG, "Background PMTiles prepare failed.", e) }
            prepareLatch.countDown()
        }.apply { name = "pmtiles-prepare" }.start()
    }

    /**
     * Best-effort wait for the bundled archive to be copied to [filesDir]. This is intended to
     * avoid first-launch races on slower devices.
     */
    fun awaitReady(context: Context, timeoutMs: Long = 120_000) {
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) return
        val appContext = context.applicationContext
        if (isCachedFileValid(appContext)) return

        if (!prepareStarted.get()) prepareAsync(appContext)
        prepareLatch.await(timeoutMs, TimeUnit.MILLISECONDS)

        // If background prep failed (or timed out), try once more synchronously.
        if (!isCachedFileValid(appContext)) {
            runCatching { resolveUrl(appContext) }
        }
    }

    fun isAssetBundled(context: Context): Boolean {
        val assetPath = BuildConfig.OVERVIEW_PMTILES_ASSET_PATH.trim()
        if (assetPath.isEmpty()) return false
        return runCatching {
            context.assets.open(assetPath).use { it.read() }
            true
        }.getOrDefault(false)
    }

    fun resolveUrl(context: Context): String? {
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) return null
        val assetPath = BuildConfig.OVERVIEW_PMTILES_ASSET_PATH.trim()
        if (assetPath.isEmpty()) return null
        return runCatching {
            val dest = cachedFile(context)
            synchronized(copyLock) {
                ensureCopiedFromAssets(context, assetPath, dest)
                requireValidPmtiles(dest)
            }
            pmtilesFileUrl(dest)
        }.onFailure { e ->
            Log.e(TAG, "Overview PMTiles unavailable; map will use tileserver only.", e)
        }.getOrNull()
    }

    private fun isCachedFileValid(context: Context): Boolean {
        val file = cachedFile(context)
        if (!file.isFile || file.length() <= 0L) return false
        return runCatching {
            requireValidPmtiles(file)
            true
        }.getOrDefault(false)
    }

    private fun requireValidPmtiles(file: File) {
        file.inputStream().use { input ->
            val header = ByteArray(7)
            val read = input.read(header)
            check(read == 7 && header.contentEquals("PMTiles".toByteArray())) {
                "Invalid PMTiles header in ${file.absolutePath}"
            }
        }
    }

    fun cachedFile(context: Context): File =
        File(context.filesDir, FILES_SUBDIR).apply { mkdirs() }.resolve(cachedFileName())

    private fun cachedFileName(): String {
        val fromConfig = File(BuildConfig.OVERVIEW_PMTILES_ASSET_PATH.trim()).name
        return fromConfig.ifEmpty { "overview.pmtiles" }
    }

    fun pmtilesFileUrl(file: File): String {
        val path = file.absolutePath
        return if (path.startsWith("/")) {
            "pmtiles://file://$path"
        } else {
            "pmtiles://file:///$path"
        }
    }

    private fun ensureCopiedFromAssets(context: Context, assetPath: String, dest: File) {
        if (dest.isFile && dest.length() > 0L) {
            if (runCatching { requireValidPmtiles(dest) }.isSuccess) return
            Log.w(TAG, "Removing invalid cached PMTiles at ${dest.absolutePath}")
            dest.delete()
        }
        val parent = dest.parentFile
        if (parent != null && !parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${cachedFileName()}.tmp")
        if (tmp.exists()) tmp.delete()
        context.assets.open(assetPath).use { input ->
            tmp.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        requireValidPmtiles(tmp)
        if (dest.exists() && !dest.delete()) {
            tmp.delete()
            error("Could not replace stale overview PMTiles at ${dest.absolutePath}")
        }
        if (!tmp.renameTo(dest)) {
            tmp.copyTo(dest, overwrite = true)
            tmp.delete()
        }
        Log.i(TAG, "Copied overview PMTiles to ${dest.absolutePath} (${dest.length()} bytes)")
    }
}

