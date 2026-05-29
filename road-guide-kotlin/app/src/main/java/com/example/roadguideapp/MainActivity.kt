package com.example.roadguideapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.roadguideapp.map.MapLibreMbTilesMap
import com.example.roadguideapp.ui.theme.RoadGuideAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadGuideAppTheme {
                MapLibreMbTilesMap(
                    lifecycle = lifecycle,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
