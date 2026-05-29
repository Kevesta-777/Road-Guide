package com.example.roadguideapp

import android.app.Application
import com.example.roadguideapp.map.MapLibreTileHttpConfigurator
import com.example.roadguideapp.map.PmtilesOverviewSource
import com.example.roadguideapp.map.TileserverBundledResources
import org.maplibre.android.MapLibre

class RoadGuideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        MapLibreTileHttpConfigurator.install(applicationContext)
        PmtilesOverviewSource.prepareAsync(applicationContext)
        Thread {
            TileserverBundledResources.ensureAssetPackMaterialized(applicationContext)
        }.apply { name = "map-asset-pack" }.start()
    }
}
