package com.example.roadguideapp.offlinegraph

import android.content.Context
import com.example.roadguideapp.R

internal fun OfflineGraphProgress.toDisplayString(context: Context): String {
    val phaseLabel = when (phase) {
        OfflineGraphEngine.ImportPhase.CopyingGraphFolder ->
            context.getString(R.string.directions_offline_import_copying_folder)
        OfflineGraphEngine.ImportPhase.ExtractingZip ->
            context.getString(R.string.directions_offline_import_extracting)
        OfflineGraphEngine.ImportPhase.LoadingGraphHopper ->
            context.getString(R.string.directions_offline_import_loading_engine)
    }
    val percentSuffix = percent?.let {
        context.getString(R.string.directions_offline_import_percent, it)
    }.orEmpty()
    val elapsedSuffix = if (elapsedMs > 0) {
        context.getString(R.string.directions_offline_import_elapsed, elapsedMs / 1000)
    } else {
        ""
    }
    return listOf(phaseLabel, detail, percentSuffix, elapsedSuffix)
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

internal fun Throwable.userMessage(): String =
    message?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
