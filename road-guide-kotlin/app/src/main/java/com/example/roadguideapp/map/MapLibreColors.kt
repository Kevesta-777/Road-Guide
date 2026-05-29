package com.example.roadguideapp.map

import android.graphics.Color

internal object MapLibreColors {

    /** MapLibre Android extrusion color is most reliable as @ColorInt, not raw `#` strings. */
    fun parseHexOrGray(hex: String): Int =
        runCatching { Color.parseColor(hex) }.getOrElse { Color.GRAY }
}
