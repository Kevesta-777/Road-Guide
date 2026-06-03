# Road Guide (Kotlin) + Headway tileserver

This Android app loads map tiles from the Headway **tileserver** (Martin) on port **8000**. Turn-by-turn geometry uses Headway **Valhalla** via the frontend nginx proxy on port **8080** (`/valhalla/route` on the road routing graph).

- **Style:** Default is `GET {tileserver}/tileserver/styles/basic/style.json` (Headway’s Martin layout). Paths inside the JSON (tiles, sprites, fonts) are rewritten to `{tileserver}/tileserver/...` — the `/tileserver` prefix is **kept** so tiles load from e.g. `{tileserver}/tileserver/data/default/{z}/{x}/{y}.pbf`. You do **not** have to ship `basic.json` in the APK unless you want offline style — then set `MAP_STYLE_ASSET_RELATIVE_PATH` in `app/build.gradle.kts` (e.g. `"map/basic.json"`).
- **Two-tier tiles (tileserver running):** Place `GreaterLondon.pmtiles` in the **project root**. Zoom **0–10** → bundled PMTiles (boundaries, main roads, place labels). Zoom **11+** → Headway tileserver only (streets, buildings, POIs, 3D). Crossover: `OVERVIEW_PMTILES_MAX_ZOOM` / `DETAIL_TILES_MIN_ZOOM` in `app/build.gradle.kts`. Sprites/glyphs still load from the tileserver when online.
- **Offline (tileserver stopped):** PMTiles-only style — map still works without the server.
- **3D buildings:** Uses the `building_3d` `fill-extrusion` layer from `basic.json` (min zoom 13; height/base from `render_height` / `render_min_height`). The app toggles that layer when you enable 3D view (camera tilt ~58°). A legacy runtime `building_extrusion` layer is removed if present.
- **Initial map bounds:** Set in Gradle (`INITIAL_MAP_MAX_BOUNDS_JSON`), not from `/static/headway-config.json`.

## Publish tileserver on the host

In Headway’s `docker-compose.yaml`, the `tileserver` service should expose port **8000** (included upstream as `8000:8000`). If 8000 is taken on the host, use e.g. `18000:8000` and set the Android base URL accordingly.

## Configure the app

Defaults in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "HEADWAY_TILESERVER_BASE_URL", "\"http://10.0.2.2:8000\"")
buildConfigField("String", "HEADWAY_VALHALLA_BASE_URL", "\"http://10.0.2.2:8080/valhalla\"")
buildConfigField("String", "INITIAL_MAP_MAX_BOUNDS_JSON", "\"\"")
buildConfigField("String", "MAP_STYLE_ASSET_RELATIVE_PATH", "\"\"") // optional: "map/basic.json"
buildConfigField("boolean", "DISABLE_FILL_EXTRUSION_ON_EMULATOR", "true")
```

On **Android Emulator**, 3D **building extrusion** is often disabled automatically (`DISABLE_FILL_EXTRUSION_ON_EMULATOR`) because MapLibre fill-extrusion can **SIGSEGV** on the emulator GLES stack; the 3D toggle still applies **camera tilt**. Use a **physical device** to verify extruded buildings.

- **Android emulator → host PC:** `http://10.0.2.2:8080` (Headway **frontend**, same as `localhost:8080` in the browser). Direct Martin is `:8000` if you need it.
- **Physical device (same Wi‑Fi as PC):** use your PC’s LAN IP, e.g. `http://192.168.1.42:8000`, and allow that host for cleartext in `app/src/main/res/xml/network_security_config.xml`, **or** use USB:

  ```bash
  adb reverse tcp:8000 tcp:8000
  adb reverse tcp:8080 tcp:8080
  ```

  and set the tileserver URL to `http://127.0.0.1:8000` and Valhalla to `http://127.0.0.1:8080/valhalla`.

### Routing (Valhalla)

Headway’s `docker-compose` must include **frontend** (`8080:8080`) and **valhalla** (internal `8002`; proxied at `/valhalla/`). The app calls `GET {HEADWAY_VALHALLA_BASE_URL}/route?json=…` with all waypoints (origin + stops) and costing `auto` / `pedestrian` / `bicycle`. If Valhalla is unreachable, the map falls back to straight-line geometry between stops.

On a physical device over Wi‑Fi, use your PC’s LAN IP for both URLs (and allow cleartext for that host in `network_security_config.xml` if needed).

### Initial map fit (fills the extract on screen)

If `INITIAL_MAP_MAX_BOUNDS_JSON` is **non-empty**, it wins. If **empty**, the app loads TileJSON from `{tileserver}/areamap` and fits the `bounds` (or uses `center` + zoom) once the map view has a real size—so the Headway extract should fill the screen.

Optional manual override (escape quotes for Gradle):

```kotlin
buildConfigField("String", "INITIAL_MAP_MAX_BOUNDS_JSON", "\"[-74.25,4.45,-73.95,4.85]\"")
```

If both are missing, the map falls back to zoom 1 over `(0, 0)`.

## Map variants (Choose Map)

The app loads styles from `{tileserver}/tileserver/styles/basic/style.json` (Standard) and similar paths for Hybrid/Satellite. The choice is persisted on device. If Hybrid or Satellite is missing on the server, the app falls back to Standard.

## Offline vs online map behavior

| Tileserver | Zoom | Tile data source |
|------------|------|------------------|
| **Running** | 0–10 | Bundled `GreaterLondon.pmtiles` only (no vector tiles from tileserver) |
| **Running** | 11+ | Headway tileserver / `openmaptiles` only (businesses, building detail, etc.) |
| **Stopped** | all | Bundled PMTiles only (offline style) |

Restart the app after starting the tileserver so it loads the dual-tier online style (or clear app storage to drop cached pre-v5 styles).

## Logcat: `TaskPersister … File error accessing recents directory`

That line comes from **Android system_server** (recent-apps persistence on the emulator), **not** from Road Guide. It is harmless noise and is **not** the cause of an app crash. Filter logcat with:

```text
package:com.example.roadguideapp level:ERROR
```

Look for `FATAL EXCEPTION`, `AndroidRuntime`, or `libc` / `libmaplibre.so`.

## Troubleshooting map crash after adding PMTiles

| Symptom | Likely cause | Fix |
|--------|----------------|-----|
| Native crash / “invalid header” / decompression errors in logcat | `pmtiles://asset://` on Android (MapLibre [#3304](https://github.com/maplibre/maplibre-native/issues/3304)) | Rebuild with latest code: overview uses `pmtiles://file://` after copying to app files. Clear app storage. |
| Blank map at low zoom | Overview file missing or copy failed | Confirm `GreaterLondon.pmtiles` in project root, rebuild APK, check logcat tag `PmtilesOverviewSource`. |
| Still broken | Stale cached style on device | Settings → Apps → Road Guide → **Clear storage**, or uninstall/reinstall. |

Filter logcat: `package:com.example.roadguideapp` and tags `PmtilesOverviewSource`, `MapDataTier`, `MapLibre`.

**PMTiles vs tileserver by zoom:** Tag `MapDataTier` logs zoom tier on camera idle and style mode on load (`Online`, `CachedHybrid`, `BundledOffline`).

**Offline detail (z11+):** After visiting a region online, the app stores the dual-tier style in `TileserverLocationDiskCache` and vector tiles/sprites in the OkHttp disk cache (`maplibre_okhttp_disk_cache`). With the tileserver stopped, reload the app — style mode `CachedHybrid` serves cached buildings/POIs for that region.

**Offline sprites/glyphs:** On first online load, tileserver sprites/glyphs are prefetched to `filesDir/map_offline_resources/`. In cached-hybrid offline mode the app uses that pack (or the OkHttp disk cache for the saved style URLs), not the APK bundle, so POI icons match cached vector tiles.

## Troubleshooting “Could not load tileserver style” (HTTP 404)

| Check | What to do |
|--------|------------|
| **Headway tileserver running** | From the host PC: `docker compose up` in your Headway repo; port **8000** must be published. |
| **Emulator reaches host** | Emulator uses `http://10.0.2.2:8000` (already the default in `.env` / `build.gradle.kts`). |
| **Style URL on host** | Open in a browser: `http://127.0.0.1:8000/tileserver/styles/basic/style.json` — must return JSON, not 404. |
| **Wrong path** | `http://127.0.0.1:8000/style/basic.json` often returns *“No such style exists”* — that is Martin’s catalog API, not Headway’s style file. The app uses `/tileserver/styles/basic/style.json` (with fallbacks). |
| **Blank map (no error)** | Often wrong **camera** (app used to fall back to 0,0 zoom 1) or wrong **host** (app must use `:8080` like the browser, not only `:8000`). Confirm `http://127.0.0.1:8080/tileserver/data/default.json` returns JSON with `bounds` or `center`. Rebuild; clear app storage to drop cached style. |
| **Rebuild app** | After changing `.env`, rebuild so `BuildConfig` picks up new URLs. |
| **Physical device** | Use your PC’s LAN IP instead of `10.0.2.2`, or `adb reverse tcp:8000 tcp:8000` and `http://127.0.0.1:8000`. |

## Offline style asset

See `app/src/main/assets/map/README.md`. Set `MAP_STYLE_ASSET_RELATIVE_PATH` in `app/build.gradle.kts` (e.g. `"map/basic.json"`) to bundle a style JSON for Standard only.

## HTTPS certificate pinning (optional)

Add to `.env`:

```
HEADWAY_CERT_PIN_SHA256=<base64-sha256-pin>
```

Pins are merged into the generated `network_security_config.xml` at build time. Leave empty for local HTTP development.

## Code layout

| Module | Role |
|--------|------|
| `MapLibreMbTilesMap.kt` | Compose UI shell (map view, sheets, chrome) |
| `PmtilesOverviewStylePatch.kt` | Merges bundled overview PMTiles + tileserver detail into style JSON |
| `MapScreenController.kt` | Search, nearby browse, place selection, style variant state |
| `MapScreenEffects.kt` | Style load, autocomplete, POI overlay side effects |
| `PlaceMetadataResolver.kt` | Hours/locality from OSM & Pelias properties |
| `NearbyResultsFilter.kt` | Open Now / chain / price filters for nearby list |
| `MapStyleVariant.kt` | Standard / Hybrid / Satellite paths + preferences |
