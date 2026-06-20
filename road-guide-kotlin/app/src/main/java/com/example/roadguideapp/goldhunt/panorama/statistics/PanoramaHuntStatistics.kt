package com.example.roadguideapp.goldhunt.panorama.statistics

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType

/**
 * Lifetime panorama hunt statistics persisted on the explorer profile.
 */
internal data class PanoramaHuntStatistics(
    val totalHunts: Int = 0,
    val huntsCompleted: Int = 0,
    val hiddenSymbolsFound: Int = 0,
    val objectsFound: Int = 0,
    val codesSolved: Int = 0,
    val panoramaPuzzlesSolved: Int = 0,
    val relicHuntsCompleted: Int = 0,
    val targetsFound: Int = 0,
    val rewardsEarned: Int = 0,
    val xpEarned: Long = 0L,
) {
    fun completedCountFor(type: PanoramaHuntType): Int = when (type) {
        PanoramaHuntType.HIDDEN_SYMBOL -> hiddenSymbolsFound
        PanoramaHuntType.OBJECT_HUNT -> objectsFound
        PanoramaHuntType.PANORAMA_PUZZLE -> panoramaPuzzlesSolved
        PanoramaHuntType.SECRET_CODE -> codesSolved
        PanoramaHuntType.RELIC_HUNT -> relicHuntsCompleted
    }

    fun withHuntStarted(): PanoramaHuntStatistics = copy(
        totalHunts = totalHunts + 1,
    )

    fun withTargetFound(targetType: PanoramaHiddenTargetType): PanoramaHuntStatistics = copy(
        targetsFound = targetsFound + 1,
        hiddenSymbolsFound = hiddenSymbolsFound +
            if (targetType == PanoramaHiddenTargetType.SYMBOL) 1 else 0,
        objectsFound = objectsFound +
            if (targetType == PanoramaHiddenTargetType.OBJECT) 1 else 0,
        codesSolved = codesSolved +
            if (targetType == PanoramaHiddenTargetType.CODE_RUNE) 1 else 0,
        panoramaPuzzlesSolved = panoramaPuzzlesSolved +
            if (targetType == PanoramaHiddenTargetType.PUZZLE_MARKER) 1 else 0,
        relicHuntsCompleted = relicHuntsCompleted +
            if (targetType == PanoramaHiddenTargetType.RELIC ||
                targetType == PanoramaHiddenTargetType.LEGENDARY_RELIC
            ) {
                1
            } else {
                0
            },
    )

    fun withHuntCompletion(
        creditsEarned: Int,
        xpEarned: Long,
    ): PanoramaHuntStatistics = copy(
        huntsCompleted = huntsCompleted + 1,
        rewardsEarned = this.rewardsEarned + creditsEarned.coerceAtLeast(0),
        xpEarned = this.xpEarned + xpEarned.coerceAtLeast(0L),
    )

    fun completedEntriesOrdered(): List<Pair<PanoramaHuntType, Int>> =
        PanoramaHuntType.ALL_ORDERED.map { it to completedCountFor(it) }

    companion object {
        val EMPTY = PanoramaHuntStatistics()
    }
}
