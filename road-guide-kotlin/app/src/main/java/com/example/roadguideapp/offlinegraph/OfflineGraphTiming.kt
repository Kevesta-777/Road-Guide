package com.example.roadguideapp.offlinegraph

import android.util.Log

internal object OfflineGraphTiming {

    @Volatile
    private var startMs: Long = 0L

    fun markStart() {
        startMs = System.currentTimeMillis()
    }

    fun elapsedMs(): Long = System.currentTimeMillis() - startMs

    fun logStep(tag: String, step: String) {
        Log.i(tag, "$step completed in ${elapsedMs()}ms")
    }
}
