package com.example.roadguideapp.map

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.roadguideapp.R

/** Fixed-size route endpoint sprites for [DirectionsWaypointMarkersOverlay]. */
internal object RouteWaypointMarkerBitmaps {

    const val MARKER_HEIGHT_DP = 60f

    fun loadImageBitmap(context: Context, @DrawableRes resId: Int): ImageBitmap {
        val options = BitmapFactory.Options().apply { inScaled = false }
        return BitmapFactory.decodeResource(context.resources, resId, options).asImageBitmap()
    }

    fun originPin(context: Context): ImageBitmap =
        loadImageBitmap(context, R.drawable.ic_route_origin_pin)

    fun destinationPin(context: Context): ImageBitmap =
        loadImageBitmap(context, R.drawable.ic_route_destination_pin)
}
