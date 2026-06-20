package com.example.roadguideapp.goldhunt

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import com.example.roadguideapp.map.MapAndroidLocation

internal class LocationSampler(
    private val context: Context,
    private val onSample: (lat: Double, lng: Double, accuracyMeters: Float) -> Unit,
) : LocationListener {

    private val appContext = context.applicationContext
    private var running = false

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return
        if (!MapAndroidLocation.hasCoarseLocationPermission(appContext)) return
        val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }
        var attached = false
        for (provider in providers) {
            if (!lm.isProviderEnabled(provider) && provider != LocationManager.PASSIVE_PROVIDER) continue
            runCatching {
                lm.requestLocationUpdates(
                    provider,
                    DiscoveryConfig.LOCATION_MIN_TIME_MS,
                    DiscoveryConfig.LOCATION_MIN_DISTANCE_METERS,
                    this,
                    Looper.getMainLooper(),
                )
                attached = true
            }
        }
        if (!attached) return
        running = true
        MapAndroidLocation.getLastKnownLatLng(appContext)?.let { latLng ->
            onSample(latLng.latitude, latLng.longitude, DiscoveryConfig.MAX_ACCURACY_METERS)
        }
    }

    fun stop() {
        if (!running) return
        val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        runCatching { lm.removeUpdates(this) }
        running = false
    }

    override fun onLocationChanged(location: Location) {
        onSample(location.latitude, location.longitude, location.accuracy)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    override fun onProviderDisabled(provider: String) = Unit
}
