package com.example.roadguideapp.goldhunt.profile

internal data class PlayerProfile(
    val totalCredits: Int,
    val totalExploredCells: Int,
    val totalDistanceM: Double,
    val discoveryPercent: Double,
    val playRegionCellCount: Long,
    val treasuresCollectedCount: Int = 0,
    val secretPlacesFoundCount: Int = 0,
)
