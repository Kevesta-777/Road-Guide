#!/usr/bin/env python3
"""Restore corrupted Gold Hunt Kotlin files."""
from pathlib import Path

GRAPH_DATA = Path(__file__).resolve().parent
REPO_ROOT = GRAPH_DATA.parent
ROOT = REPO_ROOT / "road-guide-kotlin"
MAP = ROOT / "app/src/main/java/com/example/roadguideapp/map/MapLibreMbTilesMap.kt"
GIT_MAP = GRAPH_DATA / "_git_MapLibreMbTilesMap.kt"
STRINGS = ROOT / "app/src/main/res/values/strings.xml"

FILES = {}

FILES[r"app/src/main/java/com/example/roadguideapp/goldhunt/secretplace/SecretPlaceCollectOutcome.kt"] = '''package com.example.roadguideapp.goldhunt.secretplace

internal sealed class SecretPlaceCollectOutcome {
    data class Discovered(
        val credits: Int,
        val displayName: String,
    ) : SecretPlaceCollectOutcome()

    data object AlreadyDiscovered : SecretPlaceCollectOutcome()

    data object NotRevealed : SecretPlaceCollectOutcome()

    data object NotReady : SecretPlaceCollectOutcome()
}
'''

FILES[r"app/src/main/java/com/example/roadguideapp/goldhunt/secretplace/SecretPlaceTapCollector.kt"] = '''package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.repository.SecretPlaceDiscoveryResult

internal object SecretPlaceTapCollector {
    suspend fun collectOnTap(
        repository: GoldHuntRepository,
        spec: SecretPlaceSpec,
        mapZoom: Double,
        timestampMs: Long = System.currentTimeMillis(),
    ): SecretPlaceCollectOutcome {
        repository.ensureInitialized()
        if (!SecretPlaceRevealPolicy.canDiscover(repository, mapZoom)) {
            return SecretPlaceCollectOutcome.NotRevealed
        }
        if (!SecretPlaceRevealPolicy.isVisible(spec, repository)) {
            return SecretPlaceCollectOutcome.NotRevealed
        }
        if (repository.isSecretPlaceDiscoveredCached(spec.secretPlaceId)) {
            return SecretPlaceCollectOutcome.AlreadyDiscovered
        }
        val result = repository.discoverSecretPlace(spec, timestampMs)
        if (!result.newlyDiscovered) {
            return SecretPlaceCollectOutcome.AlreadyDiscovered
        }
        return SecretPlaceCollectOutcome.Discovered(
            credits = result.creditsEarned,
            displayName = spec.displayName,
        )
    }
}
'''

FILES[r"app/src/main/java/com/example/roadguideapp/goldhunt/GoldHuntPreferences.kt"] = '''package com.example.roadguideapp.goldhunt

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

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
'''

# GoldHuntController written separately via Path read from _t184 and patch
_map_effects = GRAPH_DATA / "_goldhunt_map_effects.kt"
FILES[r"app/src/main/java/com/example/roadguideapp/goldhunt/GoldHuntMapEffects.kt"] = (
    _map_effects.read_text(encoding="utf-8") if _map_effects.exists() else ""
)

def write_files():
    for rel, content in FILES.items():
        if not content:
            continue
        path = ROOT / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        print(f"Wrote {rel}")

if __name__ == "__main__":
    write_files()
