package com.example.roadguideapp.goldhunt.storage

import android.content.Context
import com.example.roadguideapp.goldhunt.DiscoveryRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Collections

internal class DiscoveryStore(context: Context) {

    private val db = DiscoveryDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val discovered = Collections.synchronizedSet(mutableSetOf<Long>())
    private val pendingWrites = Collections.synchronizedSet(mutableSetOf<Long>())

    @Volatile
    private var loadedRegionId: String? = null

    @Volatile
    var revision: Int = 0
        private set

    suspend fun ensureLoaded(region: DiscoveryRegion) = mutex.withLock {
        if (loadedRegionId == region.id) return@withLock
        val rows = withContext(Dispatchers.IO) {
            loadRegionCells(region.id)
        }
        discovered.clear()
        discovered.addAll(rows)
        pendingWrites.clear()
        loadedRegionId = region.id
        revision++
    }

    fun isDiscovered(cellId: Long): Boolean = discovered.contains(cellId)

    fun discoveredCount(): Int = discovered.size

    /**
     * Marks cells discovered in memory; persists asynchronously.
     * @return newly discovered cell ids.
     */
    fun markDiscovered(regionId: String, cellIds: Collection<Long>): List<Long> {
        val added = ArrayList<Long>(cellIds.size)
        synchronized(discovered) {
            for (cellId in cellIds) {
                if (discovered.add(cellId)) {
                    added.add(cellId)
                    pendingWrites.add(cellId)
                }
            }
        }
        if (added.isNotEmpty()) {
            revision++
            scheduleFlush(regionId)
        }
        return added
    }

    fun flushNow(regionId: String) {
        scope.launch {
            flushPending(regionId)
        }
    }

    private fun scheduleFlush(regionId: String) {
        if (pendingWrites.size >= com.example.roadguideapp.goldhunt.DiscoveryConfig.FLUSH_PENDING_AFTER_CELLS) {
            scope.launch { flushPending(regionId) }
        }
    }

    private suspend fun flushPending(regionId: String) = mutex.withLock {
        val batch = synchronized(pendingWrites) {
            if (pendingWrites.isEmpty()) return@withLock
            pendingWrites.toList().also { pendingWrites.clear() }
        }
        val now = System.currentTimeMillis()
        withContext(Dispatchers.IO) {
            val writable = db.writableDatabase
            writable.beginTransaction()
            try {
                for (cellId in batch) {
                    writable.execSQL(
                        "INSERT OR IGNORE INTO discovered_cells (region_id, cell_id, discovered_at) VALUES (?, ?, ?)",
                        arrayOf(regionId, cellId, now),
                    )
                }
                writable.setTransactionSuccessful()
            } finally {
                writable.endTransaction()
            }
        }
    }

    private fun loadRegionCells(regionId: String): Set<Long> {
        val out = mutableSetOf<Long>()
        val cursor = db.readableDatabase.query(
            "discovered_cells",
            arrayOf("cell_id"),
            "region_id = ?",
            arrayOf(regionId),
            null,
            null,
            null,
        )
        cursor.use {
            val idx = it.getColumnIndexOrThrow("cell_id")
            while (it.moveToNext()) {
                out.add(it.getLong(idx))
            }
        }
        return out
    }
}
