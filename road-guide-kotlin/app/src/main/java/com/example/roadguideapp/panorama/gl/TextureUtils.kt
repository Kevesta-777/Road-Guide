package com.example.roadguideapp.panorama.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin

object TextureUtils {

    private val httpClient = OkHttpClient()

    fun loadFromUrl(url: String): Bitmap {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} loading panorama")
            }
            val body = response.body ?: throw IOException("Empty panorama response")
            body.byteStream().use { stream ->
                return BitmapFactory.decodeStream(stream)
                    ?: throw IOException("Failed to decode panorama from URL")
            }
        }
    }

    fun loadFromUri(context: Context, uri: Uri): Bitmap {
        context.contentResolver.openInputStream(uri).use { stream ->
            return BitmapFactory.decodeStream(stream)
                ?: throw IOException("Failed to decode image from URI")
        }
    }

    fun createPlaceholderPanorama(width: Int = 2048, height: Int = 1024): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val v = y.toFloat() / (height - 1)
            for (x in 0 until width) {
                val u = x.toFloat() / (width - 1)
                val sky = Color.rgb(
                    (40 + 120 * u).toInt().coerceIn(0, 255),
                    (80 + 80 * v).toInt().coerceIn(0, 255),
                    (160 + 60 * (1f - v)).toInt().coerceIn(0, 255),
                )
                val band = sin(u * Math.PI * 4).toFloat()
                val accent = Color.rgb(
                    (120 + 80 * band).toInt().coerceIn(0, 255),
                    (90 + 40 * cos(v * Math.PI).toFloat()).toInt().coerceIn(0, 255),
                    (70 + 30 * u).toInt().coerceIn(0, 255),
                )
                pixels[y * width + x] = blendColors(sky, accent, 0.35f)
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun blendColors(base: Int, overlay: Int, ratio: Float): Int {
        val inverse = 1f - ratio
        return Color.rgb(
            (Color.red(base) * inverse + Color.red(overlay) * ratio).toInt(),
            (Color.green(base) * inverse + Color.green(overlay) * ratio).toInt(),
            (Color.blue(base) * inverse + Color.blue(overlay) * ratio).toInt(),
        )
    }
}
