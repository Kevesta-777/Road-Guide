package com.example.roadguideapp.map

import android.content.Context
import android.util.Log
import com.example.roadguideapp.BuildConfig
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Offline map sprites/glyphs:
 * - **Cached hybrid:** tileserver prefetch (`filesDir/map_offline_resources`) or OkHttp disk cache
 *   for HTTP URLs; must match cached z11+ vector tiles.
 * - **Bundled offline:** APK [asset://] or materialized [cacheDir] `file://`.
 */
internal object TileserverBundledResources {

    private const val TAG = "TileserverBundledResources"
    private const val PREFETCH_ROOT_DIR = "map_offline_resources"
    private const val LOCAL_RESOURCES_DIR = "map_local_style_resources"
    private const val ASSET_PACK_MARKER = ".materialized"
    private const val SPRITE_BASENAME = "sprites"
    private const val MAP_ASSET_DIR = "map"
    private const val NETWORK_CONNECT_TIMEOUT_MS = 8_000
    private const val NETWORK_READ_TIMEOUT_MS = 30_000

    private val materializeLock = Any()

    private val DEFAULT_GLYPH_RANGES = listOf("0-255", "256-511", "512-767", "768-1023")

    private val REQUIRED_GLYPH_STACKS = listOf(
        "Roboto Regular",
        "Roboto Medium",
    )

    private val DEFAULT_FONT_STACKS = listOf(
        "Roboto Regular",
        "Roboto Medium",
        "Roboto Condensed Italic",
    )

    /**
     * Copies bundled sprites/fonts into [Context.cacheDir] for `file://` fallback and HTTP serving.
     */
    fun ensureAssetPackMaterialized(context: Context) {
        synchronized(materializeLock) {
            val appContext = context.applicationContext
            val root = localResourcesRoot(appContext)
            if (isMaterializedPackValid(root)) return
            root.mkdirs()
            runCatching {
                copyAssetIfPresent(
                    appContext,
                    "$MAP_ASSET_DIR/$SPRITE_BASENAME.json",
                    File(root, "$SPRITE_BASENAME.json"),
                )
                copyAssetIfPresent(
                    appContext,
                    "$MAP_ASSET_DIR/$SPRITE_BASENAME.png",
                    File(root, "$SPRITE_BASENAME.png"),
                )
                copyAssetIfPresent(
                    appContext,
                    "$MAP_ASSET_DIR/$SPRITE_BASENAME@2x.png",
                    File(root, "$SPRITE_BASENAME@2x.png"),
                )
                val spriteJson = File(root, "$SPRITE_BASENAME.json")
                if (spriteJson.isFile) {
                    spriteJson.copyTo(File(root, "$SPRITE_BASENAME@2x.json"), overwrite = true)
                }
                materializeFontTree(appContext, root)
                File(root, ASSET_PACK_MARKER).writeText("ok")
                Log.i(TAG, "Materialized bundled sprites/glyphs at ${root.absolutePath}")
            }.onFailure { e ->
                Log.w(TAG, "Bundled asset pack materialization failed.", e)
                File(root, ASSET_PACK_MARKER).delete()
            }
        }
    }

    fun prefetchForStyle(context: Context, tileserverOrigin: String, styleJson: String) {
        val origin = tileserverOrigin.trimEnd('/')
        val root = prefetchRoot(context, origin)
        root.mkdirs()
        runCatching { prefetchSprites(origin, root) }
            .onFailure { e -> Log.w(TAG, "Sprite prefetch failed.", e) }
        val fontStacks = extractFontStacks(styleJson).ifEmpty { DEFAULT_FONT_STACKS }
        runCatching { prefetchGlyphs(origin, root, fontStacks) }
            .onFailure { e -> Log.w(TAG, "Glyph prefetch failed.", e) }
        if (!isValidSpriteDirectory(root) || !isValidGlyphTree(root.resolve("font"))) {
            Log.w(TAG, "Prefetch incomplete at ${root.absolutePath}; offline will use APK assets.")
        } else {
            Log.i(TAG, "Offline map resources ready at ${root.absolutePath}")
        }
    }

    /**
     * Points style JSON at bundled sprites/glyphs for first-launch offline (PMTiles-only).
     * Priority: APK `asset://` → materialized `file://` in cacheDir.
     */
    fun applyLocalSpriteAndGlyphs(root: JSONObject, context: Context, tileserverOrigin: String) {
        val origin = tileserverOrigin.trimEnd('/')
        purgeInvalidPrefetch(context, origin)
        applyResolvedSpriteAndGlyphs(
            root = root,
            spriteUrl = resolveBundledSpriteStyleUrl(context),
            glyphsUrl = resolveBundledGlyphsStyleUrl(context),
            logLabel = "bundled offline",
        )
    }

    /**
     * For [ResolvedMapStyle.Mode.CachedHybrid]: use tileserver sprites prefetched on the last
     * online visit so POI icons match cached detail vector tiles. If prefetch is missing, keep
     * the cached style's HTTP sprite/glyph URLs so [MapLibreBundledResourceInterceptor] can serve
     * them from the OkHttp disk cache.
     */
    fun applyOfflineResourcesForCachedHybrid(
        root: JSONObject,
        context: Context,
        tileserverOrigin: String,
    ) {
        val origin = tileserverOrigin.trimEnd('/')
        purgeInvalidPrefetch(context, origin)
        val prefetched = resolvePrefetchedSpriteAndGlyphs(origin, context)
        if (prefetched != null) {
            applyResolvedSpriteAndGlyphs(
                root = root,
                spriteUrl = prefetched.spriteUrl,
                glyphsUrl = prefetched.glyphsUrl,
                logLabel = "cached hybrid (prefetch)",
            )
            return
        }
        Log.i(
            TAG,
            "Cached hybrid: keeping style sprite/glyph URLs for OkHttp disk cache " +
                "(sprite=${root.optString("sprite")})",
        )
    }

    private data class SpriteGlyphUrls(val spriteUrl: String, val glyphsUrl: String)

    private fun applyResolvedSpriteAndGlyphs(
        root: JSONObject,
        spriteUrl: String?,
        glyphsUrl: String?,
        logLabel: String,
    ) {
        if (spriteUrl != null) {
            root.put("sprite", spriteUrl)
        } else {
            Log.w(TAG, "No sprite source for $logLabel; map icons may be missing.")
        }
        if (glyphsUrl != null) {
            root.put("glyphs", glyphsUrl)
        } else {
            Log.w(TAG, "No glyphs source for $logLabel; map labels may be missing.")
        }
        Log.i(
            TAG,
            "Applied $logLabel resources: sprite=${root.optString("sprite")} " +
                "glyphs=${root.optString("glyphs")}",
        )
    }

    private fun resolvePrefetchedSpriteAndGlyphs(
        tileserverOrigin: String,
        context: Context,
    ): SpriteGlyphUrls? {
        val root = prefetchRoot(context, tileserverOrigin)
        val spriteBase = resolveSpriteFileBase(root) ?: return null
        val glyphsTemplate = resolveGlyphsFileTemplate(root.resolve("font")) ?: return null
        return SpriteGlyphUrls(fileUrl(spriteBase), glyphsTemplate)
    }

    fun hasLocalResources(context: Context, tileserverOrigin: String): Boolean {
        purgeInvalidPrefetch(context, tileserverOrigin.trimEnd('/'))
        return resolvePrefetchedSpriteAndGlyphs(tileserverOrigin.trimEnd('/'), context) != null ||
            (resolveBundledSpriteStyleUrl(context) != null && resolveBundledGlyphsStyleUrl(context) != null)
    }

    fun isTileserverSpriteOrGlyphUrl(url: HttpUrl): Boolean =
        assetPathForTileserverHttpUrl(url) != null

    /**
     * Serves tileserver sprite/glyph HTTP when offline: prefetched pack first, then APK assets.
     * Callers should try OkHttp [CacheControl.FORCE_CACHE] before this.
     */
    fun createOfflineHttpResponseBody(context: Context, url: HttpUrl): okhttp3.ResponseBody? {
        val bytes = readOfflineHttpResourceBytes(context.applicationContext, url) ?: return null
        return bytes.toResponseBody(mediaTypeForUrl(url))
    }

    private fun mediaTypeForUrl(url: HttpUrl) = when {
        url.encodedPath.endsWith(".json") -> "application/json"
        url.encodedPath.endsWith(".png") -> "image/png"
        url.encodedPath.endsWith(".pbf") -> "application/x-protobuf"
        else -> "application/octet-stream"
    }.toMediaType()

    private fun readOfflineHttpResourceBytes(context: Context, url: HttpUrl): ByteArray? {
        readPrefetchHttpResourceBytes(context, url)?.let { return it }
        return readBundledHttpResourceBytes(context, url)
    }

    private fun readPrefetchHttpResourceBytes(context: Context, url: HttpUrl): ByteArray? {
        val assetPath = assetPathForTileserverHttpUrl(url) ?: return null
        val parent = File(context.applicationContext.filesDir, PREFETCH_ROOT_DIR)
        if (!parent.isDirectory) return null
        for (slugDir in parent.listFiles().orEmpty()) {
            if (!slugDir.isDirectory) continue
            val file = localFileForAssetPath(slugDir, assetPath)
            if (file.isFile && file.length() > 0L) {
                return runCatching { file.readBytes() }.getOrNull()
            }
        }
        return null
    }

    private fun readBundledHttpResourceBytes(context: Context, url: HttpUrl): ByteArray? {
        val assetPath = assetPathForTileserverHttpUrl(url) ?: return null
        readAssetBytes(context, assetPath)?.let { return it }
        ensureAssetPackMaterialized(context)
        val file = localFileForAssetPath(localResourcesRoot(context), assetPath)
        if (file.isFile && file.length() > 0L) {
            return runCatching { file.readBytes() }.getOrNull()
        }
        return null
    }

    internal fun assetPathForTileserverHttpUrl(url: HttpUrl): String? {
        val segments = url.encodedPath.trim('/').split('/').filter { it.isNotEmpty() }
        val tileserverIndex = segments.indexOf("tileserver")
        if (tileserverIndex < 0 || tileserverIndex + 1 >= segments.size) return null
        val kind = segments[tileserverIndex + 1]
        return when (kind) {
            "sprite" -> {
                val fileName = segments.getOrNull(tileserverIndex + 2) ?: return null
                "$MAP_ASSET_DIR/$fileName"
            }
            "font" -> {
                val encodedStack = segments.getOrNull(tileserverIndex + 2) ?: return null
                val rangeFile = segments.getOrNull(tileserverIndex + 3) ?: return null
                val stack = URLDecoder.decode(encodedStack, Charsets.UTF_8.name())
                "$MAP_ASSET_DIR/font/$stack/$rangeFile"
            }
            else -> null
        }
    }

    private fun localFileForAssetPath(root: File, assetPath: String): File {
        val relative = assetPath.removePrefix("$MAP_ASSET_DIR/").removePrefix(MAP_ASSET_DIR)
            .trimStart('/')
        return root.resolve(relative)
    }

    private fun readAssetBytes(context: Context, assetPath: String): ByteArray? =
        runCatching { context.assets.open(assetPath).use { it.readBytes() } }.getOrNull()

    private fun resolveBundledSpriteStyleUrl(context: Context): String? {
        if (hasBundledAssetSprites(context)) return assetSpriteUrl()
        ensureAssetPackMaterialized(context)
        resolveSpriteFileBase(localResourcesRoot(context))?.let { return fileUrl(it) }
        return null
    }

    private fun resolveBundledGlyphsStyleUrl(context: Context): String? {
        if (hasBundledAssetFonts(context)) return assetGlyphsUrl()
        ensureAssetPackMaterialized(context)
        resolveGlyphsFileTemplate(localResourcesRoot(context).resolve("font"))?.let { return it }
        return null
    }

    private fun assetSpriteUrl(): String = "asset://$MAP_ASSET_DIR/$SPRITE_BASENAME"

    private fun assetGlyphsUrl(): String = "asset://$MAP_ASSET_DIR/font/{fontstack}/{range}.pbf"

    private fun hasBundledAssetSprites(context: Context): Boolean {
        return runCatching {
            context.assets.open("$MAP_ASSET_DIR/$SPRITE_BASENAME.json").use { it.read() }
            context.assets.open("$MAP_ASSET_DIR/$SPRITE_BASENAME.png").use { it.read() }
            true
        }.getOrDefault(false)
    }

    private fun hasBundledAssetFonts(context: Context): Boolean {
        val fontRoot = "$MAP_ASSET_DIR/font"
        return REQUIRED_GLYPH_STACKS.all { stack ->
            context.assets.list("$fontRoot/$stack")?.any { it == "0-255.pbf" } == true
        }
    }

    private fun prefetchRoot(context: Context, origin: String): File {
        val slug = TileserverLocationDiskCache.originDirectory(context, origin).name
        return File(context.applicationContext.filesDir, PREFETCH_ROOT_DIR).resolve(slug)
    }

    private fun localResourcesRoot(context: Context): File =
        context.applicationContext.cacheDir.resolve(LOCAL_RESOURCES_DIR)

    private fun purgeInvalidPrefetch(context: Context, origin: String) {
        val root = prefetchRoot(context, origin)
        if (!root.isDirectory) return
        if (isValidSpriteDirectory(root) && isValidGlyphTree(root.resolve("font"))) return
        runCatching {
            root.deleteRecursively()
            Log.i(TAG, "Removed invalid sprite/glyph prefetch at ${root.absolutePath}")
        }.onFailure { e ->
            Log.w(TAG, "Could not remove invalid prefetch directory.", e)
        }
    }

    private fun isMaterializedPackValid(root: File): Boolean {
        if (!File(root, ASSET_PACK_MARKER).isFile) return false
        return isValidSpriteDirectory(root) && isValidGlyphTree(root.resolve("font"))
    }

    internal fun isValidSpriteDirectory(root: File): Boolean {
        val json = File(root, "$SPRITE_BASENAME.json")
        val png = File(root, "$SPRITE_BASENAME.png")
        if (!json.isFile || !png.isFile) return false
        if (!isPngFile(png)) return false
        return isValidSpriteJson(json)
    }

    private fun isValidSpriteJson(file: File): Boolean {
        val text = runCatching { file.readText().trim() }.getOrNull() ?: return false
        if (!text.startsWith("{") || !text.endsWith("}")) return false
        return text.length > 2 && text.contains("\"")
    }

    internal fun isValidGlyphTree(fontDir: File): Boolean {
        if (!fontDir.isDirectory) return false
        return REQUIRED_GLYPH_STACKS.all { stack ->
            glyphRangeFile(fontDir, stack, "0-255.pbf")?.isFile == true
        }
    }

    private fun glyphRangeFile(fontDir: File, stack: String, rangeFile: String): File? {
        val literal = fontDir.resolve(stack).resolve(rangeFile)
        if (literal.isFile && literal.length() > 0L) return literal
        val encoded = fontDir.resolve(encodeFontStack(stack)).resolve(rangeFile)
        if (encoded.isFile && encoded.length() > 0L) return encoded
        return null
    }

    private fun resolveSpriteFileBase(root: File): File? {
        if (!isValidSpriteDirectory(root)) return null
        return root.resolve(SPRITE_BASENAME)
    }

    private fun resolveGlyphsFileTemplate(fontDir: File): String? {
        if (!isValidGlyphTree(fontDir)) return null
        return fileUrl(fontDir) + "/{fontstack}/{range}.pbf"
    }

    private fun fileUrl(file: File): String {
        val path = file.absolutePath.replace('\\', '/')
        return if (path.startsWith("/")) "file://$path" else "file:///$path"
    }

    private fun isPngFile(file: File): Boolean {
        if (!file.isFile || file.length() < 4L) return false
        val header = ByteArray(4)
        file.inputStream().use { input ->
            if (input.read(header) != 4) return false
        }
        return header[0] == 0x89.toByte() &&
            header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() &&
            header[3] == 0x47.toByte()
    }

    private fun materializeFontTree(context: Context, destRoot: File) {
        val assetFontDir = "$MAP_ASSET_DIR/font"
        val stacks = context.assets.list(assetFontDir)?.toList().orEmpty()
        if (stacks.isEmpty()) return
        val fontDest = destRoot.resolve("font").also { it.mkdirs() }
        for (stack in stacks) {
            val assetStackPath = "$assetFontDir/$stack"
            val ranges = context.assets.list(assetStackPath)?.toList().orEmpty()
            if (ranges.isEmpty()) continue
            for (dirName in fontDirectoryNames(stack)) {
                val stackDest = fontDest.resolve(dirName).also { it.mkdirs() }
                for (rangeFile in ranges) {
                    if (!rangeFile.endsWith(".pbf")) continue
                    copyAssetIfPresent(
                        context,
                        "$assetStackPath/$rangeFile",
                        stackDest.resolve(rangeFile),
                    )
                }
            }
        }
    }

    private fun fontDirectoryNames(stack: String): List<String> {
        val encoded = encodeFontStack(stack)
        return if (encoded == stack) listOf(stack) else listOf(stack, encoded)
    }

    private fun copyAssetIfPresent(context: Context, assetPath: String, dest: File) {
        if (dest.isFile && dest.length() > 0L) return
        runCatching {
            context.assets.open(assetPath).use { input ->
                dest.parentFile?.mkdirs()
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        }.onFailure {
            // Optional asset (e.g. sprites@2x.png).
        }
    }

    private fun prefetchSprites(origin: String, root: File) {
        val remoteBase = "$origin/tileserver/sprite/$SPRITE_BASENAME"
        downloadToFile("$remoteBase.json", File(root, "$SPRITE_BASENAME.json"))
        downloadToFile("$remoteBase.png", File(root, "$SPRITE_BASENAME.png"))
        downloadToFile("$remoteBase@2x.json", File(root, "$SPRITE_BASENAME@2x.json"))
        downloadToFile("$remoteBase@2x.png", File(root, "$SPRITE_BASENAME@2x.png"))
        val retinaJson = File(root, "$SPRITE_BASENAME@2x.json")
        if (!retinaJson.isFile) {
            File(root, "$SPRITE_BASENAME.json").takeIf { it.isFile }?.copyTo(retinaJson, overwrite = true)
        }
    }

    private fun prefetchGlyphs(origin: String, root: File, fontStacks: List<String>) {
        val fontDir = root.resolve("font").also { it.mkdirs() }
        for (stack in fontStacks) {
            for (dirName in fontDirectoryNames(stack)) {
                val stackDir = fontDir.resolve(dirName).also { it.mkdirs() }
                val encoded = encodeFontStack(stack)
                for (range in DEFAULT_GLYPH_RANGES) {
                    val url = "$origin/tileserver/font/$encoded/$range.pbf"
                    downloadToFile(url, stackDir.resolve(range))
                }
            }
        }
    }

    private fun encodeFontStack(fontStack: String): String =
        URLEncoder.encode(fontStack, Charsets.UTF_8.name()).replace("+", "%20")

    private fun extractFontStacks(styleJson: String): List<String> {
        val found = linkedSetOf<String>()
        val regex = """"text-font"\s*:\s*(\[[^\]]*])""".toRegex()
        for (match in regex.findAll(styleJson)) {
            runCatching {
                val array = JSONArray(match.groupValues[1])
                for (i in 0 until array.length()) {
                    val entry = array.optString(i).trim()
                    if (entry.isNotEmpty()) found.add(entry)
                }
            }
        }
        return found.toList()
    }

    private fun downloadToFile(url: String, dest: File) {
        if (dest.isFile && dest.length() > 0L) return
        dest.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = NETWORK_CONNECT_TIMEOUT_MS
        conn.readTimeout = NETWORK_READ_TIMEOUT_MS
        conn.instanceFollowRedirects = true
        try {
            if (conn.responseCode !in 200..299) return
            conn.inputStream.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            conn.disconnect()
        }
    }

    fun loadBundledBasicStyleJson(context: Context): String? {
        val assetPath = BuildConfig.MAP_STYLE_ASSET_RELATIVE_PATH.trim()
        if (assetPath.isEmpty()) return null
        return runCatching {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        }.getOrNull()
    }
}
