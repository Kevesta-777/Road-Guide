package com.example.roadguideapp.goldhunt.profile.ui

import android.content.Context
import com.example.roadguideapp.R
import java.util.Locale
import kotlin.math.roundToInt

internal object ExplorerProfileFormatters {
    fun formatXp(value: Long): String = String.format(Locale.getDefault(), "%,d", value)

    fun formatCredits(value: Int): String = String.format(Locale.getDefault(), "%,d", value)

    fun formatCount(value: Int): String = String.format(Locale.getDefault(), "%,d", value)

    fun formatDistance(context: Context, meters: Double): String {
        if (meters < 1_000.0) {
            return context.getString(R.string.explorer_profile_distance_m, meters.roundToInt())
        }
        val km = meters / 1_000.0
        return context.getString(R.string.explorer_profile_distance_km, km)
    }

    fun formatXpProgress(intoLevel: Long, toNext: Long): String =
        "${formatXp(intoLevel)} / ${formatXp(intoLevel + toNext)}"

    fun formatCompletionProgress(completed: Int, total: Int): String =
        "${formatCount(completed)} / ${formatCount(total)}"

    fun formatCompletionPercentage(fraction: Float): String =
        "${(fraction.coerceIn(0f, 1f) * 100f).roundToInt()}%"
}
