package com.example.roadguideapp.goldhunt.profile.level

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * Progressive explorer level curve (offline, pure math).
 *
 * Cumulative lifetime XP thresholds define each level:
 * - Level 1 begins at **0 XP** ([ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL]).
 * - **100 XP** cumulative reaches level 2, **250** → level 3, **1_500** → level 5,
 *   **10_000** → level 10, **50_000** → level 20 (design milestones).
 * - Levels between anchors are filled with log-space interpolation for smooth acceleration.
 * - Levels beyond the last anchor extrapolate along the same log slope (scalable past 100).
 *
 * Does not award XP, persist state, or grant rewards.
 */
internal object ExplorerLevelCalculator {
    const val MIN_LEVEL = 1
    const val SUPPORTED_LEVEL_COUNT = 100

    private val anchorLevels = intArrayOf(1, 2, 3, 5, 10, 20, 50, 100)
    private val anchorXp = longArrayOf(0L, 100L, 250L, 1_500L, 10_000L, 50_000L, 250_000L, 1_000_000L)

    /**
     * Minimum **lifetime total XP** required to **be** at [level].
     * Level 1 returns 0; invalid levels clamp to the supported range.
     */
    fun xpRequiredForLevel(level: Int): Long {
        val clamped = level.coerceAtLeast(MIN_LEVEL)
        if (clamped <= anchorLevels.first()) {
            return 0L
        }
        if (clamped <= anchorLevels.last()) {
            return interpolateThreshold(clamped)
        }
        return extrapolateBeyondMaxAnchor(clamped)
    }

    /**
     * Derives explorer level from **lifetime total XP** (not spendable balance).
     * Uses binary search over monotonically increasing thresholds.
     */
    fun calculateLevel(totalXp: Long): Int {
        val xp = totalXp.coerceAtLeast(0L)
        if (xp < anchorXp[1]) {
            return MIN_LEVEL
        }

        var low = MIN_LEVEL
        var high = maxLevelForXp(xp)
        while (low < high) {
            val mid = low + (high - low + 1) / 2
            if (xpRequiredForLevel(mid) <= xp) {
                low = mid
            } else {
                high = mid - 1
            }
        }
        return low
    }

    /**
     * Progress from [totalXp] toward the next level.
     * At the computed max level for the curve, [xpToNextLevel] is 0 and [progressFraction] is 1.0.
     */
    fun xpProgressToNextLevel(totalXp: Long): LevelProgress {
        val xp = totalXp.coerceAtLeast(0L)
        val level = calculateLevel(xp)
        val levelFloorXp = xpRequiredForLevel(level)
        val nextLevelXp = xpRequiredForLevel(level + 1)

        if (nextLevelXp <= levelFloorXp) {
            return LevelProgress(
                currentLevel = level,
                totalXp = xp,
                xpIntoLevel = 0L,
                xpToNextLevel = 0L,
                progressFraction = 1.0,
            )
        }

        val xpIntoLevel = (xp - levelFloorXp).coerceAtLeast(0L)
        val bandSize = nextLevelXp - levelFloorXp
        val xpToNextLevel = (bandSize - xpIntoLevel).coerceAtLeast(0L)
        val progressFraction = if (bandSize > 0L) {
            (xpIntoLevel.toDouble() / bandSize.toDouble()).coerceIn(0.0, 1.0)
        } else {
            1.0
        }

        return LevelProgress(
            currentLevel = level,
            totalXp = xp,
            xpIntoLevel = xpIntoLevel,
            xpToNextLevel = xpToNextLevel,
            progressFraction = progressFraction,
        )
    }

    private fun interpolateThreshold(level: Int): Long {
        if (level <= anchorLevels.first()) {
            return 0L
        }

        var upperIndex = 1
        while (upperIndex < anchorLevels.size && anchorLevels[upperIndex] < level) {
            upperIndex++
        }

        if (upperIndex >= anchorLevels.size) {
            return extrapolateBeyondMaxAnchor(level)
        }

        val lowerIndex = upperIndex - 1
        val lowerLevel = anchorLevels[lowerIndex]
        val upperLevel = anchorLevels[upperIndex]
        if (level == lowerLevel) {
            return anchorXp[lowerIndex]
        }
        if (level == upperLevel) {
            return anchorXp[upperIndex]
        }

        val lowerXp = anchorXp[lowerIndex].toDouble().coerceAtLeast(1.0)
        val upperXp = anchorXp[upperIndex].toDouble().coerceAtLeast(lowerXp)
        val fraction = (level - lowerLevel).toDouble() / (upperLevel - lowerLevel).toDouble()
        val interpolated = exp(ln(lowerXp) + fraction * (ln(upperXp) - ln(lowerXp)))
        return interpolated.roundToLong().coerceAtLeast(anchorXp[lowerIndex])
    }

    private fun extrapolateBeyondMaxAnchor(level: Int): Long {
        val last = anchorLevels.lastIndex
        val prev = last - 1
        val levelSpan = (anchorLevels[last] - anchorLevels[prev]).toDouble().coerceAtLeast(1.0)
        val xpRatio = anchorXp[last].toDouble() / anchorXp[prev].toDouble().coerceAtLeast(1.0)
        val logSlope = ln(xpRatio) / levelSpan
        val extraLevels = level - anchorLevels[last]
        val projected = anchorXp[last].toDouble() * exp(logSlope * extraLevels)
        return projected.roundToLong().coerceAtLeast(anchorXp[last])
    }

    private fun maxLevelForXp(totalXp: Long): Int {
        var level = anchorLevels.last()
        while (xpRequiredForLevel(level + 1) <= totalXp) {
            level++
            if (level - anchorLevels.last() > 10_000) {
                break
            }
        }
        return level
    }
}
