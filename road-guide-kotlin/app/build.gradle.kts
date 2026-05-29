plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.net.URI

fun loadDotEnv(rootDir: java.io.File): Map<String, String> {
    val envFile = rootDir.resolve(".env")
    if (!envFile.isFile) return emptyMap()
    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim().trim('"').trim('\'')
            if (key.isEmpty()) null else key to value
        }
        .toMap()
}

fun quoteForBuildConfig(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val dotEnv = loadDotEnv(rootProject.projectDir)

fun env(key: String, default: String): String =
    dotEnv[key]?.takeIf { it.isNotEmpty() } ?: default

fun hostFromUrl(url: String): String? {
    return try {
        URI(url.trim()).host?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
}

fun certificatePinsFromEnv(): List<String> {
    val raw = env("HEADWAY_CERT_PIN_SHA256", "")
    if (raw.isBlank()) return emptyList()
    return raw.split(',', ';').map { it.trim() }.filter { it.isNotEmpty() }
}

fun cleartextHostsFromEnv(): List<String> {
    val defaults = listOf("127.0.0.1", "localhost", "10.0.2.2")
    val urlKeys = listOf(
        "HEADWAY_TILESERVER_BASE_URL" to "http://10.0.2.2:8000",
        "HEADWAY_VALHALLA_BASE_URL" to "http://10.0.2.2:8080/valhalla",
        "HEADWAY_FRONTEND_BASE_URL" to "http://10.0.2.2:8080",
        "MAIN_BACKEND_BASE_URL" to "http://10.0.2.2:8090",
    )
    val fromUrls = urlKeys.mapNotNull { (key, default) ->
        val url = env(key, default)
        if (!url.startsWith("http://", ignoreCase = true)) return@mapNotNull null
        hostFromUrl(url)
    }
    return (defaults + fromUrls).distinct()
}

fun buildNetworkSecurityXml(hosts: List<String>, certPins: List<String>): String {
    val domains = hosts.joinToString("\n") { host ->
        "        <domain includeSubdomains=\"true\">$host</domain>"
    }
    val pinBlock = if (certPins.isEmpty()) {
        ""
    } else {
        val pins = certPins.joinToString("\n") { pin ->
            "            <pin digest=\"SHA-256\">$pin</pin>"
        }
        """
        |    <domain-config>
        |        <pin-set expiration="2035-12-31">
        |$pins
        |        </pin-set>
        |    </domain-config>
        """.trimMargin()
    }
    return """
        |<?xml version="1.0" encoding="utf-8"?>
        |<!-- Generated from project-root `.env` at build time. -->
        |<network-security-config>
        |    <domain-config cleartextTrafficPermitted="true">
        |$domains
        |    </domain-config>
        |$pinBlock
        |</network-security-config>
        """.trimMargin()
}

val generatedNetworkSecurityResDir =
    layout.buildDirectory.dir("generated/network_security_config/res")

tasks.register("generateNetworkSecurityConfig") {
    val envFile = rootProject.file(".env")
    inputs.file(envFile).optional()
    outputs.dir(generatedNetworkSecurityResDir)

    doLast {
        val outputDir = generatedNetworkSecurityResDir.get().asFile.resolve("xml")
        outputDir.mkdirs()
        outputDir.resolve("network_security_config.xml").writeText(
            buildNetworkSecurityXml(cleartextHostsFromEnv(), certificatePinsFromEnv()),
        )
    }
}

android {
    namespace = "com.example.roadguideapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.roadguideapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Headway URLs: override via project-root `.env` (see `.env.example`).
        // Emulator → host: 10.0.2.2. Device + USB: adb reverse → http://127.0.0.1
        buildConfigField(
            "String",
            "HEADWAY_TILESERVER_BASE_URL",
            quoteForBuildConfig(env("HEADWAY_TILESERVER_BASE_URL", "http://10.0.2.2:8080")),
        )
        buildConfigField(
            "String",
            "HEADWAY_VALHALLA_BASE_URL",
            quoteForBuildConfig(env("HEADWAY_VALHALLA_BASE_URL", "http://10.0.2.2:8080/valhalla")),
        )
        buildConfigField(
            "String",
            "HEADWAY_FRONTEND_BASE_URL",
            quoteForBuildConfig(env("HEADWAY_FRONTEND_BASE_URL", "http://10.0.2.2:8080")),
        )
        buildConfigField(
            "String",
            "MAIN_BACKEND_BASE_URL",
            quoteForBuildConfig(env("MAIN_BACKEND_BASE_URL", "http://10.0.2.2:8090")),
        )
        // Optional initial bbox `[west,south,east,north]` — e.g. "\"[-74.25,4.45,-73.95,4.85]\"" ; empty = world
        buildConfigField("String", "INITIAL_MAP_MAX_BOUNDS_JSON", "\"\"")
        // Empty = load style from tileserver only. Non-empty = e.g. "map/basic.json" under app/src/main/assets/
        buildConfigField("String", "MAP_STYLE_ASSET_RELATIVE_PATH", "\"map/basic.json\"")
        // Bundled overview PMTiles (copied from project-root GreaterLondon.pmtiles at build time).
        buildConfigField("String", "OVERVIEW_PMTILES_ASSET_PATH", "\"map/GreaterLondon.pmtiles\"")
        // Low-zoom overview from PMTiles; detail vector tiles from tileserver at this zoom and above.
        buildConfigField("int", "OVERVIEW_PMTILES_MAX_ZOOM", "11")
        buildConfigField("int", "DETAIL_TILES_MIN_ZOOM", "11")
        buildConfigField("boolean", "OVERVIEW_PMTILES_ENABLED", "true")
        // Set true to disable fill-extrusion on emulator (SIGSEGV on some GLES stacks).
        buildConfigField("boolean", "DISABLE_FILL_EXTRUSION_ON_EMULATOR", "false")
    }

    androidResources {
        noCompress += "pmtiles"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        named("main") {
            res.directories.add(generatedNetworkSecurityResDir.get().asFile.absolutePath)
        }
    }

}

val overviewPmtilesSource = rootProject.file("GreaterLondon.pmtiles")
val overviewPmtilesAsset = file("src/main/assets/map/GreaterLondon.pmtiles")

val mapSpritesJson = file("src/main/assets/map/sprites.json")
val mapSpritesRetinaJson = file("src/main/assets/map/sprites@2x.json")

tasks.register("copyMapSpriteRetinaIndex") {
    inputs.file(mapSpritesJson).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(mapSpritesRetinaJson)
    onlyIf { mapSpritesJson.isFile }
    doLast {
        mapSpritesJson.copyTo(mapSpritesRetinaJson, overwrite = true)
    }
}

tasks.register("copyOverviewPmtiles") {
    inputs.file(overviewPmtilesSource).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(overviewPmtilesAsset)
    doLast {
        if (!overviewPmtilesSource.isFile) {
            error(
                "GreaterLondon.pmtiles not found at ${overviewPmtilesSource.absolutePath}. " +
                    "Place the overview PMTiles archive in the project root.",
            )
        }
        overviewPmtilesAsset.parentFile.mkdirs()
        overviewPmtilesSource.copyTo(overviewPmtilesAsset, overwrite = true)
    }
}

tasks.named("preBuild") {
    dependsOn("generateNetworkSecurityConfig", "copyMapSpriteRetinaIndex", "copyOverviewPmtiles")
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.maplibre.android.sdk)
    implementation(libs.okhttp)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.zxing.core)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}