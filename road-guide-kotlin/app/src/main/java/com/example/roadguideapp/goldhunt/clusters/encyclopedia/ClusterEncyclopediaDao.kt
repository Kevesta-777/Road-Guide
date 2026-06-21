package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ClusterEncyclopediaDao {
    @Query("SELECT * FROM treasure_cluster_encyclopedia_entry ORDER BY catalogKey ASC")
    suspend fun all(): List<ClusterEncyclopediaEntryEntity>

    @Query("SELECT * FROM treasure_cluster_encyclopedia_entry WHERE catalogKey = :catalogKey LIMIT 1")
    suspend fun get(catalogKey: String): ClusterEncyclopediaEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ClusterEncyclopediaEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<ClusterEncyclopediaEntryEntity>)
}
