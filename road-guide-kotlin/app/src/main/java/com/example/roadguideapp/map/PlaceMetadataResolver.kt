package com.example.roadguideapp.map

import android.content.Context
import com.example.roadguideapp.R
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.Calendar

/**
 * Derives display metadata for places from OpenMapTiles / Pelias feature properties.
 */
internal object PlaceMetadataResolver {

    data class ResolvedMetadata(
        val locality: String,
        val hoursSummary: String,
        val isOpenNow: Boolean,
    )

    fun fromJsonObject(props: JsonObject, fallbackLatLngLabel: String? = null): ResolvedMetadata {
        val locality = resolveLocality(props, fallbackLatLngLabel)
        val hoursRaw = readProp(props, "opening_hours", "opening_hours:covid19")
        val hoursSummary = formatHoursSummary(hoursRaw)
        val isOpenNow = estimateOpenNow(hoursRaw)
        return ResolvedMetadata(
            locality = locality,
            hoursSummary = hoursSummary,
            isOpenNow = isOpenNow,
        )
    }

    fun fromPeliasProperties(props: JSONObject, label: String, name: String): ResolvedMetadata {
        val locality = props.optString("locality").takeIf { it.isNotBlank() }
            ?: props.optString("borough").takeIf { it.isNotBlank() }
            ?: props.optString("neighbourhood").takeIf { it.isNotBlank() }
            ?: props.optString("county").takeIf { it.isNotBlank() }
            ?: extractLocalityFromLabel(label, name)
        val hoursRaw = props.optString("opening_hours").takeIf { it.isNotBlank() }
        return ResolvedMetadata(
            locality = locality,
            hoursSummary = formatHoursSummary(hoursRaw),
            isOpenNow = estimateOpenNow(hoursRaw),
        )
    }

    fun unknownHours(context: Context): String =
        context.getString(R.string.apple_place_hours_unknown)

    private fun resolveLocality(props: JsonObject, fallbackLabel: String?): String {
        readProp(
            props,
            "addr:city",
            "addr:suburb",
            "addr:district",
            "addr:state",
            "addr:country",
            "locality",
            "borough",
            "neighbourhood",
        )?.let { return it }
        return fallbackLabel?.let { extractLocalityFromLabel(it, "") }.orEmpty()
    }

    private fun readProp(props: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            props.get(key)?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }?.let {
                return it
            }
        }
        return null
    }

    internal fun extractLocalityFromLabel(label: String, name: String): String {
        if (label.isBlank()) return ""
        val withoutName = label.removePrefix(name).trimStart(',', ' ').trim()
        if (withoutName.isBlank()) return ""
        return withoutName.split(',').map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
    }

    internal fun formatHoursSummary(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val trimmed = raw.trim()
        if (trimmed.equals("24/7", ignoreCase = true)) return "Open 24 hours"
        // Compact long OSM strings for list/detail UI.
        return trimmed
            .replace(";", "\n")
            .take(120)
            .let { if (it.length < trimmed.length) "$it…" else it }
    }

    /**
     * Best-effort open-now check for simple `HH:MM-HH:MM` or `24/7` patterns.
     */
    internal fun estimateOpenNow(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false
        if (raw.equals("24/7", ignoreCase = true)) return true
        val range = Regex("""(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})""")
            .find(raw) ?: return false
        val (sh, sm, eh, em) = range.destructured
        val start = sh.toInt() * 60 + sm.toInt()
        val end = eh.toInt() * 60 + em.toInt()
        val cal = Calendar.getInstance()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return if (end >= start) now in start..end else now >= start || now <= end
    }
}
