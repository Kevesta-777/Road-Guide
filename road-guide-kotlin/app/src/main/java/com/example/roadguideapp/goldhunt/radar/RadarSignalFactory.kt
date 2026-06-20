package com.example.roadguideapp.goldhunt.radar

/**
 * Typed builders for radar detections across gameplay target families.
 */
internal object RadarSignalFactory {
    fun treasure(
        treasureId: String,
        distanceMeters: Double,
        detectionRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? = RadarSignalCalculator.build(
        targetCategory = RadarTargetCategory.TREASURE,
        targetId = treasureId,
        distanceMeters = distanceMeters,
        maxRangeMeters = detectionRangeMeters,
        detectedTime = detectedTime,
        radarType = radarType,
    )

    fun cluster(
        clusterId: String,
        distanceMeters: Double,
        detectionRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? = RadarSignalCalculator.build(
        targetCategory = RadarTargetCategory.CLUSTER,
        targetId = clusterId,
        distanceMeters = distanceMeters,
        maxRangeMeters = detectionRangeMeters,
        detectedTime = detectedTime,
        radarType = radarType,
    )

    fun secretPlace(
        secretPlaceId: String,
        distanceMeters: Double,
        detectionRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? = RadarSignalCalculator.build(
        targetCategory = RadarTargetCategory.SECRET_PLACE,
        targetId = secretPlaceId,
        distanceMeters = distanceMeters,
        maxRangeMeters = detectionRangeMeters,
        detectedTime = detectedTime,
        radarType = radarType,
    )

    fun storyFragment(
        storyFragmentId: String,
        distanceMeters: Double,
        detectionRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? {
        val normalizedId = storyFragmentId.trim()
        if (normalizedId.isEmpty()) return null
        return RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.STORY_FRAGMENT,
            targetId = normalizedId,
            distanceMeters = distanceMeters,
            maxRangeMeters = detectionRangeMeters,
            detectedTime = detectedTime,
            radarType = radarType,
        )
    }

    fun legendaryRelic(
        legendaryRelicKey: String,
        distanceMeters: Double,
        detectionRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? {
        val normalizedKey = legendaryRelicKey.trim()
        if (normalizedKey.isEmpty()) return null
        return RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.LEGENDARY_RELIC,
            targetId = normalizedKey,
            distanceMeters = distanceMeters,
            maxRangeMeters = detectionRangeMeters,
            detectedTime = detectedTime,
            radarType = radarType,
        )
    }

    fun fromProfile(
        profile: RadarProfile,
        targetCategory: RadarTargetCategory,
        targetId: String,
        distanceMeters: Double,
        detectedTime: Long = System.currentTimeMillis(),
    ): RadarSignal? = RadarSignalCalculator.build(
        targetCategory = targetCategory,
        targetId = targetId,
        distanceMeters = distanceMeters,
        maxRangeMeters = profile.detectionProfile().detectionRangeMeters,
        detectedTime = detectedTime,
        radarType = profile.currentRadarType,
    )
}
