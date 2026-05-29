package com.example.roadguideapp.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb

/**
 * Bitmap sprites for nearby category markers (tinted circle + white glyph).
 * Drawn on a Compose overlay at [MARKER_DISPLAY_DP] so size stays fixed while zooming.
 */
internal object NearbyCategoryMarkerBitmaps {

    /** Default vector POI icon diameter on screen. */
    private const val DEFAULT_POI_DIAMETER_DP = 24f

    /** Nearby markers match default POI icon diameter on screen. */
    const val MARKER_DISPLAY_DP = DEFAULT_POI_DIAMETER_DP * 1.3f

    fun createMarkerBitmap(
        context: Context,
        category: AppleNearbyShortcut,
        density: Float = context.resources.displayMetrics.density,
    ): Bitmap {
        val sizePx = (MARKER_DISPLAY_DP * density).toInt().coerceIn(32, 192)
        return createMarkerBitmap(category, sizePx, density)
    }

    private fun createMarkerBitmap(
        category: AppleNearbyShortcut,
        sizePx: Int,
        density: Float,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val strokePx = (1.25f * density).coerceIn(1f, 3f)
        val outerRadius = sizePx / 2f - strokePx

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, outerRadius, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = category.tint.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, outerRadius - strokePx * 0.65f, fillPaint)

        val glyph = category.mapGlyph()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = sizePx * if (glyph.length == 1 && glyph[0].isLetter()) 0.44f else 0.38f
        }
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(glyph, cx, textY, textPaint)

        return bitmap
    }
}
