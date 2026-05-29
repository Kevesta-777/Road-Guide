package com.example.roadguideapp.map

import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

internal object MapViewportFit {

    /**
     * Waits until [MapView] has non-zero size (required for correct [CameraUpdateFactory.newLatLngBounds]),
     * then fits [fitBounds] with [paddingPx], or falls back to center/zoom, or world view.
     */
    fun scheduleInitialCamera(
        map: MapLibreMap,
        mapView: MapView,
        fitBounds: LatLngBounds?,
        fallbackCenter: LatLng?,
        fallbackZoom: Float?,
        paddingPx: Int,
    ) {
        val task = object : Runnable {
            override fun run() {
                if (mapView.width <= 0 || mapView.height <= 0) {
                    mapView.postOnAnimation(this)
                    return
                }
                when {
                    fitBounds != null -> {
                        try {
                            map.moveCamera(CameraUpdateFactory.newLatLngBounds(fitBounds, paddingPx))
                        } catch (_: Exception) {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(fitBounds.center, 9.0),
                            )
                        }
                    }
                    fallbackCenter != null -> {
                        val zoom = (fallbackZoom ?: 11f).toDouble()
                            .coerceIn(MapConstants.MIN_ZOOM, MapConstants.MAX_ZOOM)
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(fallbackCenter, zoom),
                        )
                    }
                    else -> {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 2.0),
                        )
                    }
                }
            }
        }
        mapView.post(task)
    }

    /**
     * Eases the camera to fit [bounds] once [mapView] has a non-zero size.
     */
    fun animateToBounds(
        map: MapLibreMap,
        mapView: MapView,
        bounds: org.maplibre.android.geometry.LatLngBounds,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int,
        durationMs: Int = DirectionsRouteAnimation.CAMERA_DURATION_MS,
    ) {
        val task = object : Runnable {
            override fun run() {
                if (mapView.width <= 0 || mapView.height <= 0) {
                    mapView.postOnAnimation(this)
                    return
                }
                try {
                    val update = CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        paddingLeft,
                        paddingTop,
                        paddingRight,
                        paddingBottom,
                    )
                    map.easeCamera(update, durationMs)
                } catch (_: Exception) {
                    runCatching {
                        map.easeCamera(
                            CameraUpdateFactory.newLatLngZoom(bounds.center, 12.0),
                            durationMs,
                        )
                    }
                }
            }
        }
        mapView.post(task)
    }
}
