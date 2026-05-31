package com.example.roadguideapp.auth

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal object QrCodeImageDecoder {

    /** Reads the first QR code payload from an image [uri], or null when none is found. */
    suspend fun decodeFirstQrRaw(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val image = runCatching { InputImage.fromFilePath(appContext, uri) }.getOrNull() ?: return@withContext null
        val scanner = BarcodeScanning.getClient()
        suspendCancellableCoroutine { cont ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (!cont.isActive) return@addOnSuccessListener
                    val raw = barcodes
                        .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                        ?.rawValue
                    cont.resume(raw)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
                .addOnCompleteListener {
                    scanner.close()
                }
        }
    }
}
