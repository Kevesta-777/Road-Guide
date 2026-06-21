package com.example.roadguideapp.goldhunt.overlays

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.maplibre.android.maps.Style

internal object GoldHuntMarkerIcons {
    /** Cap decode size for memory; displayed at [DISPLAY_ICON_SCALE] × pixel size on screen. */
    private const val MAX_DECODE_PX = 256

    /** MapLibre icon scale; lower = smaller on screen. */
    const val DISPLAY_ICON_SCALE = 0.52f

    fun treasureStyleImageId(type: TreasureType): String =
        "roadguide_treasure_icon_${type.id.lowercase()}"

    fun secretPlaceStyleImageId(): String = "roadguide_secret_place_icon"

    fun ensureTreasureIcons(style: Style, context: Context) {
        val res = context.applicationContext.resources
        for (type in TreasureType.entries) {
            registerDrawable(style, treasureStyleImageId(type), treasureDrawableRes(type), res)
        }
    }

    fun ensureSecretPlaceIcon(style: Style, context: Context) {
        registerDrawable(
            style,
            secretPlaceStyleImageId(),
            R.drawable.secret_place,
            context.applicationContext.resources,
        )
    }

    fun treasureDrawableRes(type: TreasureType): Int = when (type) {
        TreasureType.STAR -> R.drawable.treasure_star
        TreasureType.FLOWER -> R.drawable.treasure_flower
        TreasureType.CRYSTAL -> R.drawable.treasure_crystal
        TreasureType.GIFT -> R.drawable.treasure_gift
        TreasureType.HINT -> R.drawable.treasure_hint
    }

    private fun registerDrawable(
        style: Style,
        imageId: String,
        drawableRes: Int,
        res: android.content.res.Resources,
    ) {
        runCatching { style.removeImage(imageId) }
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeResource(res, drawableRes, options) ?: return
        val prepared = prepareForMapIcon(decoded)
        // Full-color sprites must NOT use SDF mode (third arg true) — that causes tiny black glyphs.
        style.addImage(imageId, prepared)
    }

    private fun prepareForMapIcon(source: Bitmap): Bitmap {
        val scaled = scaleDownPreservingAspect(source, MAX_DECODE_PX)
        return withTransparentBlackBackdrop(scaled)
    }

    private fun scaleDownPreservingAspect(source: Bitmap, maxPx: Int): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= maxPx && h <= maxPx) return source
        val scale = maxPx.toFloat() / maxOf(w, h).toFloat()
        val nw = (w * scale).toInt().coerceAtLeast(1)
        val nh = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, nw, nh, true)
    }

    /** Art assets use solid black backdrops; make them transparent so only the treasure shows. */
    private fun withTransparentBlackBackdrop(source: Bitmap): Bitmap {
        val out = if (source.config == Bitmap.Config.ARGB_8888 && source.isMutable) {
            source
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true)
        } ?: return source
        val w = out.width
        val h = out.height
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val a = p ushr 24 and 0xFF
            if (a < 8) continue
            val r = p shr 16 and 0xFF
            val g = p shr 8 and 0xFF
            val b = p and 0xFF
            if (r < 28 && g < 28 && b < 28) {
                pixels[i] = 0
            }
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
