package com.example.roadguideapp.offlinegraph

import android.util.Log
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal object OfflineGraphThreadRunner {

    private const val TAG = "OfflineGraphThreadRunner"

    @Volatile
    private var executor = newExecutor()

    fun <T> runBlockingWithTimeout(
        timeoutMs: Long,
        label: String,
        block: () -> T,
    ): Result<T> {
        val task = executor.submit(Callable { block() })
        return try {
            Result.success(task.get(timeoutMs, TimeUnit.MILLISECONDS))
        } catch (e: TimeoutException) {
            Log.e(TAG, "$label timed out after ${timeoutMs / 1000}s")
            task.cancel(true)
            replaceExecutorAfterStall()
            Result.failure(
                IllegalStateException("$label timed out. Try a smaller graph region."),
            )
        } catch (e: Exception) {
            Log.e(TAG, "$label failed", e)
            Result.failure(e)
        }
    }

    fun <T> runBlocking(label: String, block: () -> T): Result<T> {
        val task = executor.submit(Callable { block() })
        return try {
            Result.success(task.get())
        } catch (e: Exception) {
            Log.e(TAG, "$label failed", e)
            Result.failure(e)
        }
    }

    private fun replaceExecutorAfterStall() {
        runCatching { executor.shutdownNow() }
        executor = newExecutor()
    }

    private fun newExecutor() = Executors.newSingleThreadExecutor(
        ThreadFactory { runnable ->
            Thread(runnable, "GraphHopperOffline").apply { isDaemon = true }
        },
    )
}
