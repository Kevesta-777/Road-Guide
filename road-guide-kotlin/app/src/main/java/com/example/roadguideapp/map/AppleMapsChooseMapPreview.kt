package com.example.roadguideapp.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.roadguideapp.R

/**
 * Preview thumbnails for the Apple-style “Choose Map” sheet. Standard follows light/dark sheet
 * appearance; hybrid and satellite use fixed artwork.
 */
internal object AppleMapsChooseMapPreview {

    @Composable
    fun Standard(
        isLight: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val resId = if (isLight) {
            R.drawable.map_preview_standard_light
        } else {
            R.drawable.map_preview_standard_dark
        }
        MapPreviewImage(resId = resId, modifier = modifier)
    }

    @Composable
    fun Hybrid(modifier: Modifier = Modifier) {
        MapPreviewImage(resId = R.drawable.map_preview_hybrid, modifier = modifier)
    }

    @Composable
    fun Satellite(modifier: Modifier = Modifier) {
        MapPreviewImage(resId = R.drawable.map_preview_satellite, modifier = modifier)
    }
}

@Composable
private fun MapPreviewImage(
    resId: Int,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
