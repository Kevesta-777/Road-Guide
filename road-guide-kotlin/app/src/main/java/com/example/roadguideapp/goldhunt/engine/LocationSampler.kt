package com.example.roadguideapp.goldhunt.engine

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

internal data class LocationSample(
    val lat: Double,
    val lng: Double,
    val accuracyM: Float,
    val timestampMs: Long,
    val speedMps: Double? = null,
)

internal object LocationSampler {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun samples(context: Context): Flow<LocationSample> = callbackFlow {
        if (!hasPermission(context)) {
            close()
            return@callbackFlow
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationListener { location ->
            trySend(location.toSample()).isSuccess
        }
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }
        var registered = false
        for (provider in providers) {
            if (!lm.isProviderEnabled(provider) && provider != LocationManager.FUSED_PROVIDER) continue
            runCatching {
                lm.requestLocationUpdates(
                    provider,
                    GoldHuntConfig.MIN_LOCATION_INTERVAL_MS,
                    GoldHuntConfig.MIN_LOCATION_DISTANCE_M.toFloat(),
                    listener,
                    Looper.getMainLooper(),
                )
                registered = true
            }
        }
        if (!registered) {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { trySend(it.toSample()) }
        }
        awaitClose {
            lm.removeUpdates(listener)
        }
    }.flowOn(Dispatchers.Main)

    private fun Location.toSample() = LocationSample(
        lat = latitude,
        lng = longitude,
        accuracyM = accuracy.coerceAtLeast(0f),
        timestampMs = time,
        speedMps = if (hasSpeed()) speed.toDouble().coerceAtLeast(0.0) else null,
    )
}
