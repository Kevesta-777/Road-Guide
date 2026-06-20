package com.example.roadguideapp.goldhunt.panorama

/**
 * Stable identifiers for panorama hunt instances.
 * Generation consumes these builders; this module only defines the format.
 */
internal object PanoramaHuntId {
    private val VERSION_PREFIX = Regex("^ph:v(\\d+):")
    private val SEED_SUFFIX = Regex(":seed(\\d+)$")

    data class Parsed(
        val secretPlaceId: String,
        val huntTypeId: String,
        val panoramaId: String,
        val rewardSeed: Long,
    )

    fun forSecretPlace(
        secretPlaceId: String,
        huntType: PanoramaHuntType,
        panoramaId: String,
        rewardSeed: Long,
    ): String = "ph:v${PanoramaHuntSchema.VERSION}:$secretPlaceId:${huntType.id}:$panoramaId:seed$rewardSeed"

    /**
     * Parses hunt ids right-to-left so [secretPlaceId] may contain colons
     * (e.g. `sp:v1:region:CATEGORY:seed7`).
     */
    fun parse(huntId: String): Parsed? {
        val seedMatch = SEED_SUFFIX.find(huntId) ?: return null
        val rewardSeed = seedMatch.groupValues[1].toLongOrNull() ?: return null
        val withoutSeed = huntId.removeSuffix(seedMatch.value)

        val panoramaSplit = withoutSeed.lastIndexOf(':').takeIf { it > 0 } ?: return null
        val panoramaId = withoutSeed.substring(panoramaSplit + 1)
        if (panoramaId.isBlank()) return null
        val beforePanorama = withoutSeed.substring(0, panoramaSplit)

        val huntTypeSplit = beforePanorama.lastIndexOf(':').takeIf { it > 0 } ?: return null
        val huntTypeId = beforePanorama.substring(huntTypeSplit + 1)
        if (huntTypeId.isBlank()) return null
        val prefix = beforePanorama.substring(0, huntTypeSplit)

        val versionMatch = VERSION_PREFIX.find(prefix) ?: return null
        if (versionMatch.range.first != 0) return null
        val secretPlaceId = prefix.removePrefix(versionMatch.value)
        if (secretPlaceId.isBlank()) return null

        return Parsed(
            secretPlaceId = secretPlaceId,
            huntTypeId = huntTypeId,
            panoramaId = panoramaId,
            rewardSeed = rewardSeed,
        )
    }
}
