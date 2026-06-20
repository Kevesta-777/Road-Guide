package com.example.roadguideapp.goldhunt

import android.content.Context
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardRegion

internal object GoldHuntPreferences {
    private const val PREFS = "gold_hunt_prefs"
    private const val KEY_MODE_ENABLED = "mode_enabled"
    private const val KEY_INTRO_SHOWN = "workflow_intro_shown"
    private const val KEY_FIRST_STAR_ALERT_SHOWN = "workflow_first_star_shown"
    private const val KEY_FIRST_FLOWER_ALERT_SHOWN = "workflow_first_flower_shown"
    private const val KEY_FIRST_CRYSTAL_ALERT_SHOWN = "workflow_first_crystal_shown"
    private const val KEY_FIRST_GIFT_ALERT_SHOWN = "workflow_first_gift_shown"
    private const val KEY_FIRST_SECRET_ALERT_SHOWN = "workflow_first_secret_shown"
    private const val KEY_HOARD_NAME = "treasure_hoard_name"
    private const val KEY_HOARD_LAT = "treasure_hoard_lat"
    private const val KEY_HOARD_LNG = "treasure_hoard_lng"
    private const val KEY_HOARD_SEED = "treasure_hoard_seed"

    fun isModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MODE_ENABLED, false)

    fun setModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MODE_ENABLED, enabled).apply()
    }

    /** Gold Hunt mode must not survive app shutdown; clear on every cold start. */
    fun clearPersistedMode(context: Context) {
        setModeEnabled(context, false)
    }

    fun isIntroShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_INTRO_SHOWN, false)

    fun setIntroShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_INTRO_SHOWN, true).apply()
    }

    fun isFirstStarAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_STAR_ALERT_SHOWN, false)

    fun setFirstStarAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_STAR_ALERT_SHOWN, true).apply()
    }

    fun isFirstFlowerAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_FLOWER_ALERT_SHOWN, false)

    fun setFirstFlowerAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_FLOWER_ALERT_SHOWN, true).apply()
    }

    fun isFirstCrystalAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_CRYSTAL_ALERT_SHOWN, false)

    fun setFirstCrystalAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_CRYSTAL_ALERT_SHOWN, true).apply()
    }

    fun isFirstGiftAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_GIFT_ALERT_SHOWN, false)

    fun setFirstGiftAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_GIFT_ALERT_SHOWN, true).apply()
    }

    fun isFirstSecretAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_SECRET_ALERT_SHOWN, false)

    fun setFirstSecretAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_SECRET_ALERT_SHOWN, true).apply()
    }

    fun getActiveHoard(context: Context): TreasureHoardRegion? {
        val p = prefs(context)
        val name = p.getString(KEY_HOARD_NAME, null) ?: return null
        if (!p.contains(KEY_HOARD_LAT) || !p.contains(KEY_HOARD_LNG) || !p.contains(KEY_HOARD_SEED)) {
            return null
        }
        return TreasureHoardRegion(
            regionName = name,
            lat = p.getFloat(KEY_HOARD_LAT, 0f).toDouble(),
            lng = p.getFloat(KEY_HOARD_LNG, 0f).toDouble(),
            seed = p.getLong(KEY_HOARD_SEED, 0L),
        )
    }

    fun setActiveHoard(context: Context, hoard: TreasureHoardRegion) {
        prefs(context).edit()
            .putString(KEY_HOARD_NAME, hoard.regionName)
            .putFloat(KEY_HOARD_LAT, hoard.lat.toFloat())
            .putFloat(KEY_HOARD_LNG, hoard.lng.toFloat())
            .putLong(KEY_HOARD_SEED, hoard.seed)
            .apply()
    }

    fun clearActiveHoard(context: Context) {
        prefs(context).edit()
            .remove(KEY_HOARD_NAME)
            .remove(KEY_HOARD_LAT)
            .remove(KEY_HOARD_LNG)
            .remove(KEY_HOARD_SEED)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
