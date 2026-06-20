package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
internal data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalCredits: Int = 0,
    val totalExploredCellsL0: Int = 0,
    val totalDistanceM: Double = 0.0,
    val playRegionCellCountL0: Long = 0L,
    val discoveryPercent: Double = 0.0,
    val roadsDiscoveredCount: Int = 0,
    val streetsDiscoveredCount: Int = 0,
    val regionsCompletedCount: Int = 0,
    val creditsToday: Int = 0,
    val creditsTodayDate: String = "",
    val treasuresCollectedCount: Int = 0,
    val secretPlacesFoundCount: Int = 0,
    val secretPlaceCreditsEarned: Int = 0,
    val rarestSecretRarity: Int = -1,
    val secretDiscoveryStreak: Int = 0,
    val secretStreakDate: String = "",
    val updatedAt: Long = 0L,
)
