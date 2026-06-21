package com.example.roadguideapp.goldhunt.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DiscoveryDatabase(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, SCHEMA_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE discovered_cells (
                region_id TEXT NOT NULL,
                cell_id INTEGER NOT NULL,
                discovered_at INTEGER NOT NULL,
                PRIMARY KEY (region_id, cell_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX idx_discovered_region ON discovered_cells(region_id)",
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 1) {
            onCreate(db)
        }
    }

    companion object {
        private const val DB_NAME = "goldhunt_discovery.db"
        private const val SCHEMA_VERSION = 1
    }
}
