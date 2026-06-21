package com.example.roadguideapp.offlinegraph

import com.fasterxml.jackson.databind.ObjectMapper
import com.graphhopper.config.Profile
import com.graphhopper.routing.weighting.custom.CustomProfile
import com.graphhopper.util.CustomModel
import java.io.File
import java.util.Properties

/**
 * Reconstructs GraphHopper [Profile] objects from a PC-built graph-cache so
 * [OfflineGraphRouter.createHopper] profile fingerprints match the stored graph.
 */
internal object GraphCacheProfileResolver {

    private val objectMapper = ObjectMapper()

    data class StoredProfileEntry(
        val name: String,
        val version: Int,
    )

    fun loadProfiles(graphDir: File): List<Profile> {
        val props = readProperties(graphDir)
            ?: throw IllegalStateException("Graph-cache is missing the properties file.")
        val stored = parseStoredProfiles(props)
            ?: throw IllegalStateException("Graph-cache properties file is missing the profiles fingerprint.")
        val profiles = stored.map { entry ->
            matchProfile(graphDir, props, entry)
        }
        val configured = profiles.joinToString { "${it.name}|${it.version}" }
        val expected = stored.joinToString { "${it.name}|${it.version}" }
        if (configured != expected) {
            throw IllegalStateException(
                "Could not reconstruct graph profiles. Expected: $expected; got: $configured",
            )
        }
        return profiles
    }

    /** @deprecated Used only for error messages; prefer [loadProfiles]. */
    fun profileNames(graphDir: File): List<String> {
        val props = readProperties(graphDir) ?: return detectProfileIds(graphDir, null)
        return parseStoredProfiles(props)?.map { it.name }
            ?: detectProfileIds(graphDir, props)
    }

    private fun matchProfile(
        graphDir: File,
        props: Properties,
        entry: StoredProfileEntry,
    ): Profile {
        val propertyProfile = buildProfileFromProperties(entry.name, props, graphDir)
        if (propertyProfile != null && propertyProfile.version == entry.version) {
            return propertyProfile
        }
        for ((_, candidate) in profileCandidates(entry.name, props, graphDir)) {
            if (candidate.version == entry.version) {
                return candidate
            }
        }
        throw IllegalStateException(
            "Profile '${entry.name}' fingerprint ${entry.version} does not match any known configuration. " +
                "Rebuild the graph-cache with GraphHopper 7.0 or include custom_models/ and config metadata.",
        )
    }

    private fun buildProfileFromProperties(
        name: String,
        props: Properties,
        graphDir: File,
    ): Profile? {
        val prefix = "profiles.$name."
        val hasAny = props.stringPropertyNames().any { it.startsWith(prefix) }
        if (!hasAny) return null

        val vehicle = props.getProperty("${prefix}vehicle")?.trim()?.ifBlank { name } ?: name
        val weighting = props.getProperty("${prefix}weighting")?.trim()?.ifBlank { null }
        val turnCosts = props.getProperty("${prefix}turn_costs")?.trim()?.toBooleanStrictOrNull() ?: false

        val customModelJson = props.getProperty("${prefix}custom_model")?.trim()?.takeIf { it.isNotEmpty() }
        val customModelFile = props.getProperty("${prefix}custom_model_file")?.trim()?.takeIf { it.isNotEmpty() }

        return when {
            customModelJson != null -> {
                val model = parseCustomModelJson(customModelJson)
                customProfile(name, vehicle, model)
            }
            customModelFile != null -> {
                val model = loadCustomModelFile(graphDir, customModelFile)
                    ?: return null
                customProfile(name, vehicle, model)
            }
            weighting == "custom" -> {
                val model = loadCustomModelFile(graphDir, "$name.json")
                    ?: loadCustomModelFile(graphDir, "$vehicle.json")
                    ?: emptyInternalCustomModel()
                customProfile(name, vehicle, model)
            }
            weighting != null -> {
                Profile(name).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(turnCosts)
            }
            else -> null
        }
    }

    private fun profileCandidates(
        name: String,
        props: Properties,
        graphDir: File,
    ): List<Pair<String, Profile>> {
        val vehicle = props.getProperty("profiles.$name.vehicle")?.trim()?.ifBlank { name } ?: name
        val candidates = LinkedHashMap<String, Profile>()

        fun add(label: String, profile: Profile) {
            candidates.putIfAbsent(label, profile)
        }

        loadCustomModelFile(graphDir, "$name.json")?.let { model ->
            add("custom_models/$name.json", customProfile(name, vehicle, model))
        }
        loadCustomModelFile(graphDir, "$vehicle.json")?.let { model ->
            add("custom_models/$vehicle.json", customProfile(name, vehicle, model))
        }
        File(graphDir, "custom_models").listFiles()?.forEach { file ->
            if (!file.isFile || !file.name.endsWith(".json")) return@forEach
            runCatching { parseCustomModelFile(file) }.getOrNull()?.let { model ->
                add("custom_models/${file.name}", customProfile(name, vehicle, model))
            }
        }

        add("custom/distance_influence_70", customProfile(name, vehicle, distanceInfluenceModel(70.0)))
        add("custom/distance_influence_90", customProfile(name, vehicle, distanceInfluenceModel(90.0)))
        add("custom/empty", customProfile(name, vehicle, emptyInternalCustomModel()))

        for (turnCosts in listOf(false, true)) {
            for (weighting in listOf("fastest", "shortest")) {
                add(
                    "$weighting/turn_costs=$turnCosts",
                    Profile(name).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(turnCosts),
                )
            }
        }

        return candidates.entries.map { it.key to it.value }
    }

    private fun customProfile(name: String, vehicle: String, model: CustomModel): Profile {
        val profile = CustomProfile(name)
        profile.setVehicle(vehicle)
        profile.setCustomModel(model)
        return profile
    }

    private fun emptyInternalCustomModel(): CustomModel =
        CustomModel().internal()

    private fun distanceInfluenceModel(distanceInfluence: Double): CustomModel =
        CustomModel().setDistanceInfluence(distanceInfluence).internal()

    private fun parseCustomModelJson(json: String): CustomModel =
        objectMapper.readValue(json, CustomModel::class.java).internal()

    private fun parseCustomModelFile(file: File): CustomModel =
        objectMapper.readValue(file, CustomModel::class.java).internal()

    private fun loadCustomModelFile(graphDir: File, fileName: String): CustomModel? {
        if (fileName.equals("empty", ignoreCase = true)) {
            return emptyInternalCustomModel()
        }
        val direct = File(graphDir, fileName)
        if (direct.isFile) {
            return runCatching { parseCustomModelFile(direct) }.getOrNull()
        }
        val nested = File(graphDir, "custom_models/$fileName")
        if (nested.isFile) {
            return runCatching { parseCustomModelFile(nested) }.getOrNull()
        }
        return null
    }

    private fun parseStoredProfiles(props: Properties): List<StoredProfileEntry>? {
        val raw = props.getProperty("profiles")?.trim()?.ifBlank { null } ?: return null
        return raw.split(',').mapNotNull { token ->
            val trimmed = token.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            val separator = trimmed.lastIndexOf('|')
            if (separator <= 0 || separator >= trimmed.lastIndex) return@mapNotNull null
            val name = trimmed.substring(0, separator)
            val version = trimmed.substring(separator + 1).toIntOrNull() ?: return@mapNotNull null
            StoredProfileEntry(name = name, version = version)
        }.takeIf { it.isNotEmpty() }
    }

    private fun detectProfileIds(graphDir: File, props: Properties?): List<String> {
        props?.let { parseStoredProfiles(it)?.map { entry -> entry.name } }?.let { return it }

        val fromFiles = linkedSetOf<String>()
        graphDir.list()?.forEach { fileName ->
            when {
                fileName.startsWith("shortcuts_") ->
                    fromFiles.add(fileName.removePrefix("shortcuts_"))
                fileName.startsWith("nodes_ch_") ->
                    fromFiles.add(fileName.removePrefix("nodes_ch_"))
                fileName.startsWith("landmarks_") ->
                    fromFiles.add(fileName.removePrefix("landmarks_"))
            }
        }
        if (fromFiles.isNotEmpty()) return fromFiles.toList()

        props?.getProperty("prepare.ch.profiles")
            ?.split(',', ';')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        return listOf("car", "bike", "foot")
    }

    private fun readProperties(graphDir: File): Properties? {
        val file = File(graphDir, "properties")
        if (!file.isFile) return null
        return runCatching {
            Properties().apply {
                file.inputStream().buffered().use { load(it) }
            }
        }.getOrNull()
    }
}
