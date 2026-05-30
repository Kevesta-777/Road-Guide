package com.example.roadguideapp.map

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import com.example.roadguideapp.panorama.PanoramaViewerActivity
import com.example.roadguideapp.auth.AuthField
import com.example.roadguideapp.auth.OfflineAuthStore
import com.example.roadguideapp.auth.rememberAuthSheetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun BusinessDetailEditScreen(
    poiId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetTheme = rememberAuthSheetTheme()
    com.example.roadguideapp.auth.AuthPageScaffold(
        title = stringResource(R.string.business_edit_title),
        subtitle = stringResource(R.string.business_edit_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        BusinessDetailEditContent(
            poiId = poiId,
            sheetTheme = sheetTheme,
        )
    }
}

@Composable
internal fun BusinessDetailEditContent(
    poiId: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember(poiId) { mutableStateOf(true) }
    var saving by remember(poiId) { mutableStateOf(false) }
    var name by remember(poiId) { mutableStateOf("") }
    var address by remember(poiId) { mutableStateOf("") }
    var description by remember(poiId) { mutableStateOf("") }
    var media by remember(poiId) { mutableStateOf<List<BusinessPoiClient.PoiMedia>>(emptyList()) }
    var pendingUploadKind by remember { mutableStateOf<String?>(null) }

    suspend fun reload() {
        val token = OfflineAuthStore.sessionToken(context) ?: return
        when (val result = withContext(Dispatchers.IO) { BusinessPoiClient.loadPoi(poiId, token) }) {
            is BusinessPoiClient.LoadResult.Failure -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            is BusinessPoiClient.LoadResult.Success -> {
                name = result.poi.name
                address = result.poi.address
                description = result.poi.description
                media = result.media
            }
        }
    }

    LaunchedEffect(poiId) {
        loading = true
        reload()
        loading = false
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        val kind = pendingUploadKind ?: return@rememberLauncherForActivityResult
        pendingUploadKind = null
        if (uri == null) return@rememberLauncherForActivityResult
        val token = OfflineAuthStore.sessionToken(context) ?: return@rememberLauncherForActivityResult
        scope.launch {
            saving = true
            val uploadResult = withContext(Dispatchers.IO) {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "upload.jpg"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext BusinessPoiClient.UploadResult.Failure("Could not read image.")
                BusinessPoiClient.uploadMedia(
                    poiId = poiId,
                    kind = kind,
                    caption = "",
                    sortOrder = media.size,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileBytes = bytes,
                    bearerToken = token,
                )
            }
            when (uploadResult) {
                BusinessPoiClient.UploadResult.Success -> {
                    reload()
                    Toast.makeText(context, R.string.business_edit_upload_success, Toast.LENGTH_SHORT).show()
                }
                is BusinessPoiClient.UploadResult.Failure -> {
                    Toast.makeText(context, uploadResult.message, Toast.LENGTH_LONG).show()
                }
            }
            saving = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            return@Column
        }

        AuthField(
            label = stringResource(R.string.business_edit_name),
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.business_edit_name),
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthField(
            label = stringResource(R.string.business_edit_address),
            value = address,
            onValueChange = { address = it },
            placeholder = stringResource(R.string.business_edit_address),
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.business_edit_description),
            color = sheetTheme.secondaryText,
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(sheetTheme.searchFieldFill, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(color = sheetTheme.searchFieldText),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val token = OfflineAuthStore.sessionToken(context) ?: return@Button
                scope.launch {
                    saving = true
                    val result = withContext(Dispatchers.IO) {
                        BusinessPoiClient.updatePoi(
                            poiId = poiId,
                            name = name.trim(),
                            address = address.trim(),
                            description = description.trim(),
                            bearerToken = token,
                        )
                    }
                    when (result) {
                        BusinessPoiClient.SaveResult.Success -> {
                            Toast.makeText(context, R.string.business_edit_save_success, Toast.LENGTH_SHORT).show()
                        }
                        is BusinessPoiClient.SaveResult.Failure -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    saving = false
                }
            },
            enabled = !saving && name.isNotBlank() && address.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.business_edit_save))
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.business_edit_media_title),
            fontWeight = FontWeight.SemiBold,
            color = sheetTheme.primaryText,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                enabled = !saving,
                onClick = {
                    pendingUploadKind = "photo"
                    pickImageLauncher.launch("image/*")
                },
            ) {
                Text(text = stringResource(R.string.business_edit_upload_photo))
            }
            OutlinedButton(
                enabled = !saving,
                onClick = {
                    pendingUploadKind = "panorama"
                    pickImageLauncher.launch("image/*")
                },
            ) {
                Text(text = stringResource(R.string.business_edit_upload_panorama))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (media.isEmpty()) {
            Text(
                text = stringResource(R.string.business_edit_no_media),
                color = sheetTheme.secondaryText,
            )
        } else {
            media.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.kind, fontWeight = FontWeight.Medium, color = sheetTheme.primaryText)
                        if (item.caption.isNotBlank()) {
                            Text(text = item.caption, color = sheetTheme.secondaryText)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.kind == "panorama") {
                            OutlinedButton(
                                enabled = !saving,
                                onClick = {
                                    val imageUrl = BusinessPoiClient.resolveMediaUrl(item.url)
                                    context.startActivity(
                                        Intent(context, PanoramaViewerActivity::class.java).apply {
                                            putExtra(PanoramaViewerActivity.EXTRA_IMAGE_URL, imageUrl)
                                            putExtra(PanoramaViewerActivity.EXTRA_TITLE, name.trim().ifBlank { poiId })
                                        },
                                    )
                                },
                            ) {
                                Text(text = stringResource(R.string.business_edit_view_panorama))
                            }
                        }
                        OutlinedButton(
                            enabled = !saving,
                            onClick = {
                                val token = OfflineAuthStore.sessionToken(context) ?: return@OutlinedButton
                                scope.launch {
                                    saving = true
                                    val result = withContext(Dispatchers.IO) {
                                        BusinessPoiClient.deleteMedia(poiId, item.id, token)
                                    }
                                    when (result) {
                                        BusinessPoiClient.DeleteResult.Success -> {
                                            reload()
                                            Toast.makeText(
                                                context,
                                                R.string.business_edit_delete_success,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                        is BusinessPoiClient.DeleteResult.Failure -> {
                                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    saving = false
                                }
                            },
                        ) {
                            Text(text = stringResource(R.string.business_edit_delete))
                        }
                    }
                }
            }
        }
    }
}
