package com.example.roadguideapp.map

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import org.maplibre.android.maps.MapView

internal fun applyMapBackdropBlur(mapView: MapView?, blurRadiusPx: Float) {
    if (mapView == null) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        mapView.setRenderEffect(null)
        return
    }
    mapView.setRenderEffect(
        if (blurRadiusPx <= 0f) {
            null
        } else {
            RenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP)
        },
    )
}
