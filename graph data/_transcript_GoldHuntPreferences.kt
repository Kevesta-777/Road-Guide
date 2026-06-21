package com.example.roadguideapp.goldhunt

import android.content.Context

internal object GoldHuntPreferences {
    private const val PREFS = "gold_hunt_prefs"
    private const val KEY_MODE_ENABLED = "mode_enabled"
    private const val KEY_INTRO_SHOWN = "workflow_intro_shown"
    private const val KEY_FIRST_AREA_ALERT_SHOWN = "workflow_first_area_shown"
    private const val KEY_FIRST_STAR_ALERT_SHOWN = "workflow_first_star_shown"
    private const val KEY_FIRST_FLOWER_ALERT_SHOWN = "workflow_first_flower_shown"

    fun isModeEnabled(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MODE_ENABLED, false)
    }

    fun setModeEnabled(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MODE_ENABLED, enabled)
            .apply()
    }

    fun isIntroShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_INTRO_SHOWN, false)

    fun setIntroShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_INTRO_SHOWN, true).apply()
    }

    fun isFirstAreaAlertShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_AREA_ALERT_SHOWN, false)

    fun setFirstAreaAlertShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_FIRST_AREA_ALERT_SHOWN, true).apply()
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

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
