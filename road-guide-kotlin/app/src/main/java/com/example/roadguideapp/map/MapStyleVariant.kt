package com.example.roadguideapp.map

import android.content.Context
import com.example.roadguideapp.R

/** Map appearance variants loaded from the Headway tileserver. */
internal enum class MapStyleVariant(
    val stylePath: String,
    val labelRes: Int,
) {
    Standard(
        stylePath = AppMapStyle.TILESERVER_STYLE_PATH,
        labelRes = R.string.apple_map_style_standard,
    ),
    Hybrid(
        stylePath = AppMapStyle.TILESERVER_HYBRID_STYLE_PATH,
        labelRes = R.string.apple_map_style_hybrid,
    ),
    Satellite(
        stylePath = AppMapStyle.TILESERVER_SATELLITE_STYLE_PATH,
        labelRes = R.string.apple_map_style_satellite,
    ),
    ;

    companion object {
        fun fromPersistedName(name: String?): MapStyleVariant =
            entries.firstOrNull { it.name == name } ?: Standard
    }
}

internal object MapStylePreferences {
    private const val PREFS = "map_style_prefs"
    private const val KEY_VARIANT = "variant"

    fun read(context: Context): MapStyleVariant {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return MapStyleVariant.fromPersistedName(prefs.getString(KEY_VARIANT, null))
    }

    fun write(context: Context, variant: MapStyleVariant) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VARIANT, variant.name)
            .apply()
    }
}
