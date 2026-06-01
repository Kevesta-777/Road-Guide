package com.example.roadguideapp.offlinegraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

internal object OfflineGraphEngine {

    private const val TAG = "OfflineGraphEngine"
    private const val COPY_TIMEOUT_MS = 45 * 60 * 1000L

    private val loadMutex = Mutex()

    @Volatile
    private var loadInProgress: Boolean = false

    fun isLoaded(): Boolean = OfflineGraphRouter.isReady()

    fun isLoadInProgress(): Boolean = loadInProgress

    fun hasSavedGraph(context: Context): Boolean =
        OfflineGraphStorage.getActiveGraphPath(context) != null

    suspend fun ensureReady(
        context: Context,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Boolean = restoreSavedGraph(context, onProgress)

    suspend fun restoreSavedGraph(
        context: Context,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Boolean = loadMutex.withLock {
        if (isLoaded()) return true
        val path = OfflineGraphStorage.getActiveGraphPath(context) ?: return false
        loadGraphBlocking(path, onProgress).isSuccess
    }

    suspend fun importGraphFolder(
        context: Context,
        resolver: ContentResolver,
        folderTreeUri: Uri,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Result<String> = withContext(Dispatchers.IO) {
        importGraphBundle(context, onProgress) { dest ->
            copyGraphToDest("Graph folder copy", dest) {
                GraphBundleImporter.copyGraphFolderFromTree(
                    context = context,
                    treeUri = folderTreeUri,
                    destDir = dest,
                ) { copyProgress ->
                    onProgress(
                        OfflineGraphProgress(
                            phase = ImportPhase.CopyingGraphFolder,
                            detail = copyProgress.detail,
                            percent = copyProgress.percent,
                            elapsedMs = OfflineGraphTiming.elapsedMs(),
                        ),
                    )
                }
            }
        }
    }

    suspend fun importGraphZip(
        context: Context,
        resolver: ContentResolver,
        zipUri: Uri,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Result<String> = withContext(Dispatchers.IO) {
        importGraphBundle(context, onProgress) { dest ->
            val zipSize = resolver.openFileDescriptor(zipUri, "r")?.use { it.statSize } ?: -1L
            copyGraphToDest("ZIP extract", dest) {
                GraphBundleImporter.copyGraphZip(
                    resolver = resolver,
                    zipUri = zipUri,
                    destDir = dest,
                    zipSizeBytes = zipSize,
                ) { extractProgress ->
                    onProgress(
                        OfflineGraphProgress(
                            phase = ImportPhase.ExtractingZip,
                            detail = extractProgress.detail,
                            percent = extractProgress.percent,
                            elapsedMs = OfflineGraphTiming.elapsedMs(),
                        ),
                    )
                }
            }
        }
    }

    private suspend fun importGraphBundle(
        context: Context,
        onProgress: (OfflineGraphProgress) -> Unit,
        copyToDest: (dest: File) -> Result<File>,
    ): Result<String> {
        OfflineGraphTiming.markStart()
        prepareDeviceForGraphImport(context)
        val dest = OfflineGraphStorage.folderForNewImport(context)
        return try {
            copyToDest(dest).fold(
                onSuccess = { graphDir -> finalizeImport(context, graphDir, onProgress) },
                onFailure = { error ->
                    dest.deleteRecursively()
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Graph import failed", e)
            runCatching { dest.deleteRecursively() }
            Result.failure(e)
        }
    }

    private fun copyGraphToDest(
        label: String,
        dest: File,
        copy: () -> Unit,
    ): Result<File> {
        val copyResult = OfflineGraphThreadRunner.runBlockingWithTimeout(
            timeoutMs = COPY_TIMEOUT_MS,
            label = label,
        ) {
            copy()
            Unit
        }
        if (copyResult.isFailure) {
            return Result.failure(
                copyResult.exceptionOrNull() ?: IllegalStateException("$label failed."),
            )
        }
        OfflineGraphTiming.logStep(TAG, label)
        return Result.success(dest)
    }

    private fun finalizeImport(
        context: Context,
        dest: File,
        onProgress: (OfflineGraphProgress) -> Unit,
    ): Result<String> {
        GraphBundleImporter.logGraphCacheSummary(dest)
        OfflineGraphMemory.releaseHeapBeforeGraphLoad(context.applicationContext)
        val loadOutcome = loadGraphBlocking(dest.absolutePath, onProgress)
        if (loadOutcome.isFailure) {
            dest.deleteRecursively()
            return Result.failure(
                loadOutcome.exceptionOrNull()
                    ?: IllegalStateException("GraphHopper could not load the graph."),
            )
        }
        OfflineGraphStorage.saveActiveGraphPath(context, dest.absolutePath)
        Log.i(TAG, "Imported graph to ${dest.absolutePath}")
        return Result.success(dest.absolutePath)
    }

    private fun loadGraphBlocking(
        graphFolderPath: String,
        onProgress: (OfflineGraphProgress) -> Unit,
    ): Result<Unit> {
        if (isLoaded()) return Result.success(Unit)
        loadInProgress = true
        OfflineGraphTiming.markStart()
        try {
            onProgress(
                OfflineGraphProgress(
                    phase = ImportPhase.LoadingGraphHopper,
                    detail = "Starting GraphHopper…",
                    percent = null,
                    elapsedMs = 0L,
                ),
            )
            val threadResult = OfflineGraphThreadRunner.runBlocking("GraphHopper.load") {
                OfflineGraphRouter.loadGraphBlocking(graphFolderPath, onProgress)
            }
            return when {
                threadResult.isFailure -> Result.failure(
                    threadResult.exceptionOrNull()
                        ?: IllegalStateException("GraphHopper.load failed on graph thread."),
                )
                threadResult.getOrNull()?.isSuccess == true -> Result.success(Unit)
                else -> Result.failure(
                    threadResult.getOrNull()?.exceptionOrNull()
                        ?: IllegalStateException("GraphHopper.load failed."),
                )
            }
        } finally {
            loadInProgress = false
        }
    }

    private fun prepareDeviceForGraphImport(context: Context) {
        OfflineGraphThreadRunner.runBlocking("shutdown-before-import") {
            OfflineGraphRouter.shutdownBlocking()
        }
        OfflineGraphStorage.clearActiveGraph(context)
        OfflineGraphStorage.deleteAllImportedGraphs(context)
        OfflineGraphMemory.releaseHeapBeforeGraphLoad(context.applicationContext)
    }

    enum class ImportPhase {
        CopyingGraphFolder,
        ExtractingZip,
        LoadingGraphHopper,
    }
}
