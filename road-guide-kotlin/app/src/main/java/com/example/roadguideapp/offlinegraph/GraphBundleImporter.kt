package com.example.roadguideapp.offlinegraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

internal object GraphBundleImporter {

    private const val TAG = "GraphBundleImporter"

    fun isValidGraphCache(dir: File): Boolean {
        if (!dir.isDirectory) return false
        return File(dir, "edges").exists() && File(dir, "nodes").exists()
    }

    fun logGraphCacheSummary(dir: File) {
        val files = dir.list()?.sorted()?.take(12).orEmpty()
        Log.i(TAG, "Graph-cache at ${dir.absolutePath}: ${files.joinToString(", ")}")
    }

    fun copyGraphZip(
        resolver: ContentResolver,
        zipUri: Uri,
        destDir: File,
        zipSizeBytes: Long,
        onProgress: (CopyProgress) -> Unit,
    ) {
        destDir.mkdirs()
        resolver.openInputStream(zipUri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                var filesCopied = 0
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(destDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out -> zip.copyTo(out) }
                        filesCopied++
                        if (filesCopied % 10 == 0) {
                            onProgress(CopyProgress("Extracting… ($filesCopied files)", null))
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("Cannot open ZIP")
        flattenSingleGraphCacheSubdir(destDir)
        if (!isValidGraphCache(destDir)) {
            error("ZIP does not contain a valid GraphHopper graph-cache (need edges, nodes).")
        }
        onProgress(CopyProgress("Extract complete", 100))
    }

    fun copyGraphFolderFromTree(
        context: Context,
        treeUri: Uri,
        destDir: File,
        onProgress: (CopyProgress) -> Unit,
    ) {
        destDir.mkdirs()
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: error("Cannot access selected folder.")
        copyDocumentTree(context.contentResolver, root, destDir, onProgress)
        flattenSingleGraphCacheSubdir(destDir)
        if (!isValidGraphCache(destDir)) {
            error("Selected folder is not a valid GraphHopper graph-cache (need edges, nodes).")
        }
        onProgress(CopyProgress("Copy complete", 100))
    }

    private fun copyDocumentTree(
        resolver: ContentResolver,
        doc: DocumentFile,
        destDir: File,
        onProgress: (CopyProgress) -> Unit,
    ) {
        if (doc.isDirectory) {
            destDir.mkdirs()
            doc.listFiles().forEach { child ->
                val name = child.name ?: return@forEach
                if (child.isDirectory) {
                    copyDocumentTree(resolver, child, File(destDir, name), onProgress)
                } else {
                    copyDocumentFile(resolver, child, File(destDir, name), onProgress)
                }
            }
        } else {
            copyDocumentFile(resolver, doc, destDir, onProgress)
        }
    }

    private fun copyDocumentFile(
        resolver: ContentResolver,
        doc: DocumentFile,
        dest: File,
        onProgress: (CopyProgress) -> Unit,
    ) {
        val name = doc.name ?: return
        dest.parentFile?.mkdirs()
        resolver.openInputStream(doc.uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        } ?: error("Cannot read $name")
        onProgress(CopyProgress("Copied $name", null))
    }

    private fun flattenSingleGraphCacheSubdir(destDir: File) {
        if (isValidGraphCache(destDir)) return
        val subdirs = destDir.listFiles()?.filter { it.isDirectory }.orEmpty()
        if (subdirs.size == 1 && isValidGraphCache(subdirs[0])) {
            val nested = subdirs[0]
            nested.listFiles()?.forEach { child ->
                child.copyTo(File(destDir, child.name), overwrite = true)
            }
            nested.deleteRecursively()
        }
    }
}
