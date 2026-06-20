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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntGenerator
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntWorldSeed
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntCompletionManager
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntSession
import com.example.roadguideapp.goldhunt.panorama.interaction.PanoramaHuntInteractionFeedback
import com.example.roadguideapp.goldhunt.panorama.interaction.PanoramaHuntInteractionLayer
import com.example.roadguideapp.goldhunt.panorama.interaction.PanoramaHuntTapEvent
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntStatisticsManager
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetGenerator
import com.example.roadguideapp.panorama.gl.PanoramaGLSurfaceView
import com.example.roadguideapp.panorama.gl.TextureUtils
import com.example.roadguideapp.ui.theme.RoadGuideAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Full-screen equirectangular panorama viewer (merged from panorama-native). */
class PanoramaViewerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)?.trim().orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE)?.trim().orEmpty()
        val panoramaId = intent.getStringExtra(EXTRA_PANORAMA_ID)?.trim().orEmpty()
        val secretPlaceId = intent.getStringExtra(EXTRA_SECRET_PLACE_ID)?.trim().orEmpty()
        val worldSeed = intent.getStringExtra(EXTRA_WORLD_SEED)?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: PanoramaHuntWorldSeed.DEFAULT
        if (imageUrl.isEmpty()) {
            finish()
            return
        }

        setContent {
            RoadGuideAppTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val panoramaView = remember { PanoramaGLSurfaceView(context) }
                val completionManager = remember { PanoramaHuntCompletionManager.get(context) }
                val statisticsManager = remember { PanoramaHuntStatisticsManager.get(context) }
                var huntSession by remember { mutableStateOf<PanoramaHuntSession?>(null) }

                LaunchedEffect(panoramaId, secretPlaceId, worldSeed) {
                    if (panoramaId.isEmpty() || secretPlaceId.isEmpty()) {
                        huntSession = null
                        return@LaunchedEffect
                    }
                    val hunt = PanoramaHuntGenerator.generate(
                        panoramaId = panoramaId,
                        secretPlaceId = secretPlaceId,
                        worldSeed = worldSeed,
                    )
                    val targets = PanoramaHiddenTargetGenerator.generateSet(hunt)
                    statisticsManager.recordHuntStarted(hunt)
                    val progress = completionManager.loadProgress(hunt, targets)
                    huntSession = PanoramaHuntSession(
                        hunt = hunt,
                        targets = targets,
                        interaction = PanoramaHuntInteractionLayer(
                            targetSet = targets,
                            preCollectedSlots = progress.collectedSlotIndices,
                        ),
                    )
                }

                LaunchedEffect(panoramaView, huntSession) {
                    val session = huntSession
                    panoramaView.onPanoramaTap = if (session == null) {
                        null
                    } else {
                        { screenX, screenY ->
                            scope.launch {
                                val result = session.interaction.onTap(
                                    PanoramaHuntTapEvent(
                                        screenX = screenX,
                                        screenY = screenY,
                                        viewState = panoramaView.panoramaRenderer.viewState(),
                                    ),
                                )
                                PanoramaHuntInteractionFeedback.showToast(context, result)
                                completionManager.processTapResult(
                                    hunt = session.hunt,
                                    targetSet = session.targets,
                                    result = result,
                                )
                            }
                        }
                    }
                }

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
        const val EXTRA_PANORAMA_ID = "panoramaId"
        const val EXTRA_SECRET_PLACE_ID = "secretPlaceId"
        const val EXTRA_WORLD_SEED = "worldSeed"
    }
}
