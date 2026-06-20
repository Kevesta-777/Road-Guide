package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory

internal object RadarRewardStatisticsMapper {
    fun toDomain(entity: RadarRewardStatisticsEntity): RadarRewardStatistics =
        RadarRewardStatistics(
            totalXpEarned = entity.totalXpEarned.coerceAtLeast(0L),
            totalCreditsEarned = entity.totalCreditsEarned.coerceAtLeast(0),
            rewardedScans = entity.rewardedScans.coerceAtLeast(0),
            rewardedDetections = entity.rewardedDetections.coerceAtLeast(0),
            treasureDetectionsRewarded = entity.treasureDetectionsRewarded.coerceAtLeast(0),
            clusterDetectionsRewarded = entity.clusterDetectionsRewarded.coerceAtLeast(0),
            secretPlaceDetectionsRewarded = entity.secretPlaceDetectionsRewarded.coerceAtLeast(0),
            storyFragmentDetectionsRewarded = entity.storyFragmentDetectionsRewarded.coerceAtLeast(0),
            legendaryRelicDetectionsRewarded = entity.legendaryRelicDetectionsRewarded.coerceAtLeast(0),
            schemaVersion = entity.schemaVersion,
            updatedAtMs = entity.updatedAtMs,
        )

    fun toEntity(
        stats: RadarRewardStatistics,
        id: Int = RadarRewardSchema.STATISTICS_SINGLETON_ID,
    ): RadarRewardStatisticsEntity = RadarRewardStatisticsEntity(
        id = id,
        totalXpEarned = stats.totalXpEarned.coerceAtLeast(0L),
        totalCreditsEarned = stats.totalCreditsEarned.coerceAtLeast(0),
        rewardedScans = stats.rewardedScans.coerceAtLeast(0),
        rewardedDetections = stats.rewardedDetections.coerceAtLeast(0),
        treasureDetectionsRewarded = stats.treasureDetectionsRewarded.coerceAtLeast(0),
        clusterDetectionsRewarded = stats.clusterDetectionsRewarded.coerceAtLeast(0),
        secretPlaceDetectionsRewarded = stats.secretPlaceDetectionsRewarded.coerceAtLeast(0),
        storyFragmentDetectionsRewarded = stats.storyFragmentDetectionsRewarded.coerceAtLeast(0),
        legendaryRelicDetectionsRewarded = stats.legendaryRelicDetectionsRewarded.coerceAtLeast(0),
        schemaVersion = stats.schemaVersion,
        updatedAtMs = stats.updatedAtMs,
    )

    fun applyBundle(
        current: RadarRewardStatistics,
        grantedSignalRewards: List<RadarSignalReward>,
        grantedXp: Long,
        grantedCredits: Int,
        scanRecorded: Boolean,
        timestampMs: Long,
    ): RadarRewardStatistics {
        if (!scanRecorded) return current
        var treasure = current.treasureDetectionsRewarded
        var cluster = current.clusterDetectionsRewarded
        var secretPlace = current.secretPlaceDetectionsRewarded
        var story = current.storyFragmentDetectionsRewarded
        var relic = current.legendaryRelicDetectionsRewarded
        for (reward in grantedSignalRewards) {
            when (reward.targetCategory) {
                RadarTargetCategory.TREASURE -> treasure++
                RadarTargetCategory.CLUSTER -> cluster++
                RadarTargetCategory.SECRET_PLACE -> secretPlace++
                RadarTargetCategory.STORY_FRAGMENT -> story++
                RadarTargetCategory.LEGENDARY_RELIC -> relic++
            }
        }
        return current.copy(
            totalXpEarned = current.totalXpEarned + grantedXp,
            totalCreditsEarned = current.totalCreditsEarned + grantedCredits,
            rewardedScans = current.rewardedScans + 1,
            rewardedDetections = current.rewardedDetections + grantedSignalRewards.size,
            treasureDetectionsRewarded = treasure,
            clusterDetectionsRewarded = cluster,
            secretPlaceDetectionsRewarded = secretPlace,
            storyFragmentDetectionsRewarded = story,
            legendaryRelicDetectionsRewarded = relic,
            updatedAtMs = timestampMs,
        )
    }
}
