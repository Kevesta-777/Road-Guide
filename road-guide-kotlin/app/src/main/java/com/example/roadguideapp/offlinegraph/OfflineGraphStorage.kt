package com.example.roadguideapp.offlinegraph

import android.content.Context
import java.io.File
import java.util.UUID

internal object OfflineGraphStorage {

    private const val PREFS = "offline_graph"
    private const val KEY_ACTIVE_PATH = "active_graph_path"

    fun getActiveGraphPath(context: Context): String? {
        val path = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_PATH, null)
            ?: return null
        val dir = File(path)
        return if (dir.isDirectory && GraphBundleImporter.isValidGraphCache(dir)) path else null
    }

    fun saveActiveGraphPath(context: Context, path: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACTIVE_PATH, path)
            .apply()
    }

    fun clearActiveGraph(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ACTIVE_PATH)
            .apply()
    }

    fun folderForNewImport(context: Context): File {
        val root = File(context.filesDir, "graph-imports")
        root.mkdirs()
        return File(root, "import-${UUID.randomUUID()}")
    }

    fun deleteAllImportedGraphs(context: Context) {
        val root = File(context.filesDir, "graph-imports")
        if (root.exists()) root.deleteRecursively()
    }
}
