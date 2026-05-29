package com.example.roadguideapp.map

import android.os.Build
import com.example.roadguideapp.BuildConfig

/**
 * MapLibre [FillExtrusionLayer] / fill-extrusion triggers native SIGSEGV on some GLES stacks,
 * notably the Android Emulator’s “OpenGL ES Translator” (see user crash dumps in RenderThread).
 */
internal object MapRenderSupport {

    fun safeToShowFillExtrusionBuildings(): Boolean {
        if (!BuildConfig.DISABLE_FILL_EXTRUSION_ON_EMULATOR) return true
        return !isLikelyAndroidEmulator()
    }

    fun safeToUse3dCameraTilt(): Boolean {
        if (!BuildConfig.DISABLE_FILL_EXTRUSION_ON_EMULATOR) return true
        return !isLikelyAndroidEmulator()
    }

    /**
     * MapLibre fill-extrusion + bearing changes can SIGSEGV on the emulator GLES translator.
     * Keep 3D tilt but disable finger-rotation there; compass reset still runs programmatically.
     */
    fun allowsFingerRotationIn3d(): Boolean = !isLikelyAndroidEmulator()

    fun preferTextureViewRendering(): Boolean = isLikelyAndroidEmulator()

    fun isLikelyAndroidEmulator(): Boolean {
        val fp = Build.FINGERPRINT
        if (fp.startsWith("generic") || fp.startsWith("unknown")) return true
        if (fp.contains("emulator", ignoreCase = true)) return true
        if (fp.contains("sdk_gphone", ignoreCase = true)) return true
        val model = Build.MODEL
        if (model.contains("google_sdk", ignoreCase = true)) return true
        if (model.contains("Emulator", ignoreCase = true)) return true
        if (model.contains("Android SDK built for x86", ignoreCase = true)) return true
        if (model.contains("sdk_gphone", ignoreCase = true)) return true
        val product = Build.PRODUCT
        if (product.contains("sdk", ignoreCase = true)) return true
        if (product.contains("emulator", ignoreCase = true)) return true
        val device = Build.DEVICE
        if (device.contains("generic", ignoreCase = true)) return true
        if (device.contains("emulator", ignoreCase = true)) return true
        if (Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)) return true
        if (Build.BOARD.contains("goldfish", ignoreCase = true)) return true
        if (Build.BOARD.contains("ranchu", ignoreCase = true)) return true
        if (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) return true
        return when (Build.HARDWARE) {
            "goldfish", "ranchu" -> true
            else -> false
        }
    }
}
