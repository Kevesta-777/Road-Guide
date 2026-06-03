package com.example.roadguideapp.offlinegraph

import android.content.Context

internal object OfflineGraphMemory {

    fun releaseHeapBeforeGraphLoad(context: Context) {
        runCatching { context.cacheDir } // touch context; optional GC hint
        System.gc()
    }
}
