package com.example.roadguideapp.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.roadguideapp.R

/** Mode-specific navigation puck sprites for 3D turn-by-turn. */
internal object NavigationVehicleIcon {

    fun styleImageId(travelMode: DirectionsTravelMode): String = when (travelMode) {
        DirectionsTravelMode.Drive -> "roadguide-nav-drive-v1"
        DirectionsTravelMode.Walk -> "roadguide-nav-walk-v1"
        DirectionsTravelMode.Bicycle -> "roadguide-nav-bike-v1"
    }

    fun loadBitmap(context: Context, travelMode: DirectionsTravelMode): Bitmap {
        val resId = when (travelMode) {
            DirectionsTravelMode.Drive -> R.drawable.drive_navigation
            DirectionsTravelMode.Walk -> R.drawable.walk_navigation
            DirectionsTravelMode.Bicycle -> R.drawable.bike_navigation
        }
        val options = BitmapFactory.Options().apply { inScaled = false }
        return BitmapFactory.decodeResource(context.resources, resId, options)
    }
}
