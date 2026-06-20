package com.example.roadguideapp.goldhunt.clusters

/**
 * Stable identifiers for procedurally generated treasure clusters.
 * Generation consumes these builders; this module only defines the format.
 */
internal object TreasureClusterId {
    private val PARSE_REGEX = Regex(
        "^tc:v(\\d+):(.+):L(\\d+):(\\d+):(\\d+):seed(\\d+)$",
    )

    data class Parsed(
        val playRegionId: String,
        val level: Int,
        val tileX: Int,
        val tileY: Int,
        val clusterSeed: Long,
    )

    fun forL1Anchor(
        playRegionId: String,
        regionL1Id: String,
        clusterSeed: Long,
    ): String =
        "tc:v${ClusterSchema.VERSION}:$playRegionId:$regionL1Id:seed$clusterSeed"

    fun parse(clusterId: String): Parsed? {
        val match = PARSE_REGEX.matchEntire(clusterId) ?: return null
        return Parsed(
            playRegionId = match.groupValues[2],
            level = match.groupValues[3].toInt(),
            tileX = match.groupValues[4].toInt(),
            tileY = match.groupValues[5].toInt(),
            clusterSeed = match.groupValues[6].toLong(),
        )
    }
}
