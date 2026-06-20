package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treasure_cluster_encyclopedia_entry")
internal data class ClusterEncyclopediaEntryEntity(
    @PrimaryKey val catalogKey: String,
    val status: String,
    val firstDiscoveredAtMs: Long? = null,
    val completedAtMs: Long? = null,
    val totalRewardsEarned: Int = 0,
    val discoveredCount: Int = 0,
    val completedCount: Int = 0,
    val achievementKey: String? = null,
    val storyFragmentId: String? = null,
    val schemaVersion: Int = ClusterEncyclopediaSchema.VERSION,
    val extensionJson: String = ClusterEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
