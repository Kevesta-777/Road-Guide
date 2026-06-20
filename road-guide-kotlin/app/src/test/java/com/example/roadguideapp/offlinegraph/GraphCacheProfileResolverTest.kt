package com.example.roadguideapp.offlinegraph

import com.graphhopper.config.Profile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GraphCacheProfileResolverTest {

    @Test
    fun loadProfiles_matchesFastestFingerprintsFromPropertiesLine() {
        val dir = Files.createTempDirectory("gh-cache-fastest").toFile()
        try {
            File(dir, "edges").mkdir()
            File(dir, "nodes").mkdir()
            File(dir, "shortcuts_car").mkdir()
            val car = Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false)
            File(dir, "properties").writeText(
                """
                profiles=${car.name}|${car.version}
                profiles.car.vehicle=car
                profiles.car.weighting=fastest
                profiles.car.turn_costs=false
                """.trimIndent(),
            )
            val loaded = GraphCacheProfileResolver.loadProfiles(dir)
            assertEquals(listOf("car"), loaded.map { it.name })
            assertEquals(car.version, loaded.single().version)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun loadProfiles_matchesCustomDistanceInfluenceFingerprint() {
        val dir = Files.createTempDirectory("gh-cache-custom").toFile()
        try {
            File(dir, "edges").mkdir()
            File(dir, "nodes").mkdir()
            File(dir, "shortcuts_car").mkdir()
            val model = com.graphhopper.util.CustomModel().setDistanceInfluence(70.0).internal()
            val expected = com.graphhopper.routing.weighting.custom.CustomProfile("car").apply {
                setVehicle("car")
                setCustomModel(model)
            }
            File(dir, "properties").writeText(
                """
                profiles=car|${expected.version}
                """.trimIndent(),
            )
            val loaded = GraphCacheProfileResolver.loadProfiles(dir)
            assertEquals(expected.version, loaded.single().version)
            assertEquals("custom", loaded.single().weighting)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun profileNames_prefersStoredProfilesLine() {
        val dir = Files.createTempDirectory("gh-cache-names").toFile()
        try {
            File(dir, "properties").writeText("profiles=car|1,bike|2,foot|3")
            assertEquals(listOf("car", "bike", "foot"), GraphCacheProfileResolver.profileNames(dir))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun loadProfiles_readsWeightingFromProperties() {
        val dir = Files.createTempDirectory("gh-cache-props").toFile()
        try {
            File(dir, "edges").mkdir()
            File(dir, "nodes").mkdir()
            File(dir, "shortcuts_bike").mkdir()
            val bike = Profile("bike").setVehicle("bike").setWeighting("shortest").setTurnCosts(false)
            File(dir, "properties").writeText(
                """
                profiles=bike|${bike.version}
                profiles.bike.name=bike
                profiles.bike.vehicle=bike
                profiles.bike.weighting=shortest
                """.trimIndent(),
            )
            val loaded = GraphCacheProfileResolver.loadProfiles(dir)
            assertEquals("bike", loaded.single().name)
            assertEquals("shortest", loaded.single().weighting)
            assertEquals(bike.version, loaded.single().version)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun loadProfiles_matchesTypicalGh7CustomGraphFingerprints() {
        val dir = Files.createTempDirectory("gh-cache-gh7").toFile()
        try {
            File(dir, "edges").mkdir()
            File(dir, "nodes").mkdir()
            File(dir, "shortcuts_car").mkdir()
            File(dir, "shortcuts_bike").mkdir()
            File(dir, "shortcuts_foot").mkdir()
            File(dir, "properties").writeText(
                """
                profiles=car|-1232697972,bike|-1273290306,foot|-468386082
                """.trimIndent(),
            )
            val loaded = GraphCacheProfileResolver.loadProfiles(dir)
            assertEquals(listOf("car", "bike", "foot"), loaded.map { it.name })
            assertEquals(-1232697972, loaded[0].version)
            assertEquals(-1273290306, loaded[1].version)
            assertEquals(-468386082, loaded[2].version)
        } finally {
            dir.deleteRecursively()
        }
    }
}
