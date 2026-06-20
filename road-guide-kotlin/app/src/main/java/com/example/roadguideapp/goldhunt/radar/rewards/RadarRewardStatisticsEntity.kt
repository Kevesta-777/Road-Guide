package com.example.roadguideapp.goldhunt.radar.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radar_reward_statistics")
internal data class RadarRewardStatisticsEntity(
    @PrimaryKey val id: Int = RadarRewardSchema.STATISTICS_SINGLETON_ID,
    val totalXpEarned: Long = 0L,
    val totalCreditsEarned: Int = 0,
    val rewardedScans: Int = 0,
    val rewardedDetections: Int = 0,
    val treasureDetectionsRewarded: Int = 0,
    val clusterDetectionsRewarded: Int = 0,
    val secretPlaceDetectionsRewarded: Int = 0,
    val storyFragmentDetectionsRewarded: Int = 0,
    val legendaryRelicDetectionsRewarded: Int = 0,
    val schemaVersion: Int = RadarRewardSchema.VERSION,
    val updatedAtMs: Long = 0L,
)
