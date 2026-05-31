package com.example.roadguideapp.auth

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.roadguideapp.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ScanFriendQrScreen(
    onBack: () -> Unit,
    onFriendAdded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val sheetTheme = rememberAuthSheetTheme()
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var pendingPayload by remember { mutableStateOf<FriendQrPayloadData?>(null) }
    var scanLocked by remember { mutableStateOf(false) }
    var isProcessingImage by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    fun processDecodedQr(raw: String) {
        if (scanLocked || isProcessingImage) return
        val payload = FriendQrPayload.decode(raw)
        if (payload == null) {
            Toast.makeText(context, R.string.friends_error_invalid_qr, Toast.LENGTH_SHORT).show()
            return
        }
        scanLocked = true
        scope.launch {
            val resolvedName = payload.displayName?.trim()?.takeIf { it.isNotEmpty() }
                ?: when (
                    val lookup = withContext(Dispatchers.IO) {
                        OfflineFriendsStore.resolveProfile(context, payload.profileId)
                    }
                ) {
                    is ResolveProfileResult.Success -> lookup.displayName
                    is ResolveProfileResult.Failure -> {
                        Toast.makeText(
                            context,
                            friendErrorMessage(context, lookup.error),
                            Toast.LENGTH_SHORT,
                        ).show()
                        scanLocked = false
                        null
                    }
                }
            if (resolvedName != null) {
                pendingPayload = payload.copy(displayName = resolvedName)
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null || scanLocked || isProcessingImage) return@rememberLauncherForActivityResult
        isProcessingImage = true
        scope.launch {
            val raw = QrCodeImageDecoder.decodeFirstQrRaw(context, uri)
            isProcessingImage = false
            if (raw.isNullOrBlank()) {
                Toast.makeText(context, R.string.friends_error_no_qr_in_image, Toast.LENGTH_SHORT).show()
            } else {
                processDecodedQr(raw)
            }
        }
    }

    pendingPayload?.let { payload ->
        val displayLabel = payload.displayName
            ?: stringResource(R.string.friends_unknown_name)
        AlertDialog(
            onDismissRequest = {
                pendingPayload = null
                scanLocked = false
            },
            title = { Text(stringResource(R.string.friends_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.friends_confirm_message,
                        displayLabel,
                        payload.profileId,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                OfflineFriendsStore.addFriend(
                                    context,
                                    payload.profileId,
                                    payload.displayName,
                                )
                            }
                            when (result) {
                                AddFriendResult.Success -> {
                                    Toast.makeText(context, R.string.friends_added, Toast.LENGTH_SHORT).show()
                                    pendingPayload = null
                                    scanLocked = false
                                    runOnMainThread(context) { onFriendAdded() }
                                }
                                is AddFriendResult.Failure -> {
                                    Toast.makeText(
                                        context,
                                        friendErrorMessage(context, result.error),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    pendingPayload = null
                                    scanLocked = false
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.friends_add_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingPayload = null
                    scanLocked = false
                }) {
                    Text(stringResource(R.string.friends_add_cancel))
                }
            },
        )
    }

    AuthPageScaffold(
        title = stringResource(R.string.friends_scan_title),
        subtitle = stringResource(R.string.friends_scan_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        if (!hasCameraPermission) {
            Text(
                text = stringResource(R.string.friends_camera_permission_message),
                color = sheetTheme.secondaryText,
            )
            AuthPrimaryButton(
                text = stringResource(R.string.friends_grant_camera),
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                sheetTheme = sheetTheme,
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            AuthGroupedCard(sheetTheme = sheetTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, sheetTheme.divider.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                ) {
                    QrCameraPreview(
                        enabled = !scanLocked && pendingPayload == null && !isProcessingImage,
                        onQrDecoded = { raw ->
                            mainExecutor.execute { processDecodedQr(raw) }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.friends_scan_qr_hint),
                color = sheetTheme.secondaryText,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        AuthSecondaryButton(
            text = stringResource(
                if (isProcessingImage) {
                    R.string.friends_upload_qr_image_processing
                } else {
                    R.string.friends_upload_qr_image
                },
            ),
            onClick = { pickImageLauncher.launch("image/*") },
            sheetTheme = sheetTheme,
            enabled = !scanLocked && !isProcessingImage && pendingPayload == null,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.friends_upload_qr_image_hint),
            color = sheetTheme.tertiaryText,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun QrCameraPreview(
    enabled: Boolean,
    onQrDecoded: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val appContext = context.applicationContext
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val scanInFlight = remember { AtomicBoolean(false) }

    DisposableEffect(lifecycleOwner, enabled, previewView) {
        val view = previewView
        if (!enabled || view == null) {
            return@DisposableEffect onDispose { }
        }
        var cameraProvider: ProcessCameraProvider? = null
        val cameraProviderFuture = ProcessCameraProvider.getInstance(appContext)
        cameraProviderFuture.addListener({
            val provider = runCatching { cameraProviderFuture.get() }.getOrNull() ?: return@addListener
            cameraProvider = provider
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { imageProxy ->
                if (!enabled || scanInFlight.get()) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                val input = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees,
                )
                barcodeScanner.process(input)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            if (barcode.format != Barcode.FORMAT_QR_CODE) continue
                            val raw = barcode.rawValue ?: continue
                            if (!scanInFlight.compareAndSet(false, true)) return@addOnSuccessListener
                            mainExecutor.execute {
                                onQrDecoded(raw)
                                scanInFlight.set(false)
                            }
                            break
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            } catch (_: Exception) {
                scanInFlight.set(false)
            }
        }, mainExecutor)

        onDispose {
            scanInFlight.set(false)
            cameraProvider?.unbindAll()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
        modifier = modifier,
        update = { view ->
            if (previewView !== view) previewView = view
        },
    )
}
