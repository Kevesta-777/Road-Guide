package com.example.roadguideapp.map

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import org.maplibre.android.geometry.LatLng

internal object MapAndroidLocation {

    fun hasCoarseLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun getLastKnownLatLng(context: Context): LatLng? {
        if (!hasCoarseLocationPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val candidates = mutableListOf<Location>()
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }
        providers.forEach { provider ->
            if (!lm.isProviderEnabled(provider) && provider != LocationManager.PASSIVE_PROVIDER) return@forEach
            runCatching { lm.getLastKnownLocation(provider) }.getOrNull()?.let { candidates += it }
        }
        val best = candidates.maxByOrNull { it.time } ?: return null
        return LatLng(best.latitude, best.longitude)
    }
}
