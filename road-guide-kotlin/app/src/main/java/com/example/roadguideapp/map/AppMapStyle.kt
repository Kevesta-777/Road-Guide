package com.example.roadguideapp.map

/**
 * Client-owned map presentation constants aligned with Headway `basic.json`.
 */
internal object AppMapStyle {

    /** Must match `basic.json` vector source id for areamap tiles. */
    const val OPENMAPTILES_SOURCE_ID = "openmaptiles"

    /** Bundled low-zoom PMTiles source id (see [PmtilesOverviewStylePatch]). */
    const val OVERVIEW_SOURCE_ID = PmtilesOverviewStylePatch.OVERVIEW_SOURCE_ID

    /**
     * 3D buildings from Headway `basic.json` (`fill-extrusion`, source-layer `building`).
     * Visibility is toggled when the user enables 3D view.
     */
    const val BUILDING_3D_LAYER_ID = "building_3d"

    /** Flat building footprints in `basic.json`; faded by the style above z15.5. */
    const val BUILDING_LAYER_ID = "building"

    /** Legacy runtime layer id from earlier app versions (removed on style load). */
    const val LEGACY_BUILDING_EXTRUSION_LAYER_ID = "building_extrusion"

    /**
     * Headway tileserver (Martin) publishes the basic style here.
     * See https://github.com/headwaymaps/libheadway — not `/style/basic.json` (Martin catalog id).
     */
    const val TILESERVER_STYLE_PATH = "/tileserver/styles/basic/style.json"

    /** Alternate paths tried if the primary Headway URL is missing (older Martin/nginx layouts). */
    val TILESERVER_STYLE_PATH_FALLBACKS: List<String> = listOf(
        "/style/basic.json",
        "/tileserver/style/basic.json",
    )

    /** Optional Headway variants (fall back to Standard if missing on server). */
    const val TILESERVER_HYBRID_STYLE_PATH = "/tileserver/styles/hybrid/style.json"
    const val TILESERVER_SATELLITE_STYLE_PATH = "/tileserver/styles/satellite/style.json"

    const val BUILDING_SOURCE_LAYER = "building"

    /** Matches `building_3d.minzoom` in `basic.json`. */
    const val BUILDING_3D_MIN_ZOOM = 13f

    /** Matches `building_3d.maxzoom` in `basic.json`. */
    const val BUILDING_3D_MAX_ZOOM = 24f
}
