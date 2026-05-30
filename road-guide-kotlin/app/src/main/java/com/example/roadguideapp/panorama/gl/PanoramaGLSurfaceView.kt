package com.example.roadguideapp.panorama.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.abs

class PanoramaGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    val panoramaRenderer = PanoramaRenderer()

    private var previousX = 0f
    private var previousY = 0f
    private var isDragging = false

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val fovDelta = (1f - scaleFactor) * 20f
                panoramaRenderer.adjustFieldOfView(fovDelta)
                return true
            }
        },
    )

    init {
        setEGLContextClientVersion(2)
        setRenderer(panoramaRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun requestRenderPanorama() {
        requestRender()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (scaleDetector.isInProgress) {
            isDragging = false
            requestRender()
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDragging || event.pointerCount > 1) {
                    return true
                }
                val deltaX = event.x - previousX
                val deltaY = event.y - previousY
                if (abs(deltaX) > 0.5f || abs(deltaY) > 0.5f) {
                    panoramaRenderer.addLookFromDrag(deltaX, deltaY)
                    previousX = event.x
                    previousY = event.y
                    requestRender()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return true
    }
}
