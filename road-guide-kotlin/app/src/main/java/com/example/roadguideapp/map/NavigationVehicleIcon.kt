package com.example.roadguideapp.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader

/** Sygic-style white chevron on a dark translucent ground disk. */
internal object NavigationVehicleIcon {
    private const val CACHE_KEY = "roadguide-nav-vehicle-sygic-v1"

    fun styleImageId(): String = CACHE_KEY

    fun createBitmap(sizePx: Int = 192): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx * 0.58f
        val diskR = sizePx * 0.36f

        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx,
                cy,
                diskR * 1.42f,
                intArrayOf(0x88000000.toInt(), 0x44000000.toInt(), 0x00000000),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(cx, cy, diskR * 1.35f, haloPaint)

        val diskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - diskR * 0.12f,
                cy - diskR * 0.18f,
                diskR * 1.12f,
                intArrayOf(0xAA6A6A6A.toInt(), 0x99484848.toInt(), 0x88303030.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(cx, cy, diskR, diskPaint)

        val diskRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.01f
            color = 0x44FFFFFF
        }
        canvas.drawCircle(cx, cy, diskR - diskRim.strokeWidth * 0.5f, diskRim)

        val arrowH = sizePx * 0.4f
        val arrowW = sizePx * 0.3f
        val tipY = cy - arrowH * 0.55f
        val tailY = cy + arrowH * 0.2f
        val notchY = cy - arrowH * 0.02f

        val arrowPath = Path().apply {
            moveTo(cx, tipY)
            lineTo(cx + arrowW * 0.54f, tailY)
            lineTo(cx, notchY)
            lineTo(cx - arrowW * 0.54f, tailY)
            close()
        }

        val arrowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - arrowW * 0.55f,
                tipY,
                cx + arrowW * 0.55f,
                tailY,
                intArrayOf(0xFFFFFFFF.toInt(), 0xFFF8F8F8.toInt(), 0xFFE0E0E0.toInt()),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawPath(arrowPath, arrowFill)

        val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.018f
            color = 0xE6FFFFFF.toInt()
            strokeCap = Paint.Cap.ROUND
        }
        val ridge = Path().apply {
            moveTo(cx, tipY + arrowH * 0.1f)
            lineTo(cx, notchY - arrowH * 0.06f)
        }
        canvas.drawPath(ridge, ridgePaint)

        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.012f
            color = 0x66000000
        }
        canvas.drawPath(arrowPath, edgePaint)

        return bmp
    }
}
