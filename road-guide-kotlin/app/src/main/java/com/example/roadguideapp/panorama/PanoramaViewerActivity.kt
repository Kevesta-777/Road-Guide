package com.example.roadguideapp.panorama

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.roadguideapp.R
import com.example.roadguideapp.panorama.gl.PanoramaGLSurfaceView
import com.example.roadguideapp.panorama.gl.TextureUtils
import com.example.roadguideapp.ui.theme.RoadGuideAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Full-screen equirectangular panorama viewer (merged from panorama-native). */
class PanoramaViewerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)?.trim().orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE)?.trim().orEmpty()
        if (imageUrl.isEmpty()) {
            finish()
            return
        }

        setContent {
            RoadGuideAppTheme {
                val context = LocalContext.current
                val panoramaView = remember { PanoramaGLSurfaceView(context) }

                LaunchedEffect(imageUrl) {
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            TextureUtils.loadFromUrl(imageUrl)
                        }
                        panoramaView.queueEvent {
                            panoramaView.panoramaRenderer.queueBitmap(bitmap)
                            panoramaView.requestRenderPanorama()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.panorama_viewer_load_error, e.message ?: "unknown"),
                            Toast.LENGTH_LONG,
                        ).show()
                        finish()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(title.ifEmpty { context.getString(R.string.panorama_viewer_title) })
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = context.getString(R.string.panorama_viewer_close),
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            ),
                        )
                    },
                ) { innerPadding ->
                    AndroidView(
                        factory = { panoramaView },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE_URL = "imageUrl"
        const val EXTRA_TITLE = "title"
    }
}
