package com.example.roadguideapp.goldhunt.relics

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import kotlin.math.roundToLong

/**
 * Schema metadata for the legendary relic foundation.
 *
 * Bump [VERSION] when adding persistence columns; use [extensionJson] payloads
 * on future relic progress entities before adding Room columns.
 */
internal object RelicSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    /** Reserved JSON keys for relic progress and collection extension payloads. */
    object ExtensionKeys {
        const val CATEGORY = "category"
        const val RELIC_KEY = "relicKey"
        const val ACHIEVEMENT_KEY = "achievementKey"
        const val STORY_FRAGMENT_KEY = "storyFragmentKey"
        const val SECRET_PLACE_CATEGORY_ID = "secretPlaceCategoryId"
        const val EXPLORER_LEVEL_AT_UNLOCK = "explorerLevelAtUnlock"
        const val PROGRESS_FRACTION = "progressFraction"
        const val SOURCE_DOMAIN = "sourceDomain"
        const val BADGE_KEY = "badgeKey"
        const val TITLE_KEY = "titleKey"
        const val POWER_KEY = "powerKey"
        const val PIECES_COLLECTED = "piecesCollected"
        const val SOURCE_KEY = "sourceKey"
        const val PIECE_NUMBER = "pieceNumber"
    }

    /**
     * Cross-profile extension bucket for relic collection state.
     * Mirrors [ExplorerProfileSchema.ExtensionKeys.LEGENDARY_RELICS].
     */
    object ProfileExtensionBuckets {
        const val LEGENDARY_RELICS = ExplorerProfileSchema.ExtensionKeys.LEGENDARY_RELICS
    }

    /** Colon-delimited relic piece key namespace for shard tracking. */
    object PieceKeys {
        const val PREFIX = "relic_piece"

        fun forPiece(relicId: String, pieceNumber: Int): String =
            "$PREFIX:$relicId:$pieceNumber"
    }

    /** Colon-delimited relic key namespace for the unified catalog layer. */
    object RelicKeys {
        const val PREFIX = "legendary_relic"

        fun forCategory(category: RelicCategory): String =
            "${PREFIX}_${category.id.lowercase()}"

        fun forCategoryId(categoryId: String): String? =
            RelicCategory.fromId(categoryId)?.let { forCategory(it) }

        fun milestone(category: RelicCategory, milestone: Int): String =
            "${PREFIX}:${category.id.lowercase()}:milestone_$milestone"

        fun catalogIndex(category: RelicCategory, index: Int): String =
            "${PREFIX}:${category.id.lowercase()}:catalog_$index"
    }

    /** Count-milestone keys for scalable 50 / 100 / 500+ relic families. */
    object MilestoneKeys {
        fun relicId(category: RelicCategory, milestone: Int): String =
            RelicKeys.milestone(category, milestone)
    }

    /** Indexed catalog keys for large relic families contributed by providers. */
    object CatalogKeys {
        fun forCategory(category: RelicCategory, index: Int): String =
            RelicKeys.catalogIndex(category, index)
    }

    /** Domain-specific relic key namespaces consumed by registry providers. */
    object DomainKeys {
        object SecretPlace {
            const val PREFIX = "legendary_relic_secret_place"

            fun forCategory(category: SecretPlaceCategory): String =
                "${PREFIX}_${category.id.lowercase()}"
        }

        object Panorama {
            const val PREFIX = "legendary_relic_panorama_hunt"

            fun forType(type: PanoramaHuntType): String =
                "${PREFIX}_${type.id.lowercase()}"
        }

        object SeasonalEvent {
            const val PREFIX = "legendary_relic_seasonal_event"

            fun forType(type: SeasonalEventType): String =
                "${PREFIX}_${type.id.lowercase()}"
        }

        object Cluster {
            const val PREFIX = "legendary_relic_treasure_cluster"

            fun forType(type: ClusterType): String =
                "${PREFIX}_${type.id.lowercase()}"
        }

        object Radar {
            const val PREFIX = "legendary_relic_radar"

            fun forType(type: RadarType): String =
                "${PREFIX}_${type.id.lowercase()}"
        }

        object TreasureRarityKeys {
            const val PREFIX = "legendary_relic_treasure_rarity"

            fun forRarity(rarity: TreasureRarity): String =
                "${PREFIX}_${rarity.name.lowercase()}"
        }
    }

    /** Achievement hook namespace reserved for relic-category progression. */
    object AchievementKeys {
        const val PREFIX = "relic_category"

        fun firstDiscovery(category: RelicCategory): String =
            "$PREFIX:${category.id.lowercase()}:first_discovery"

        fun collector(category: RelicCategory): String =
            "$PREFIX:${category.id.lowercase()}:collector"

        fun mastery(category: RelicCategory): String =
            "$PREFIX:${category.id.lowercase()}:mastery"

        fun completeAllCategories(): String = "$PREFIX:all_categories_complete"
    }

    /** Badge hook namespace reserved for relic completion rewards. */
    object BadgeKeys {
        const val PREFIX = "badge_relic"

        fun forRelic(relicId: String): String = "$PREFIX:$relicId"
    }

    /** Title hook namespace reserved for relic completion rewards. */
    object TitleKeys {
        const val PREFIX = "title_relic"

        fun forRelic(relicId: String): String = "$PREFIX:$relicId"
    }

    /** Relic power hook namespace reserved for future passive bonuses. */
    object PowerKeys {
        const val PREFIX = "relic_power"

        fun forRelic(relicId: String): String = "$PREFIX:$relicId"
    }

    /** Cosmetic hook namespace reserved for future relic completion cosmetics. */
    object CosmeticKeys {
        const val PREFIX = "cosmetic_relic"

        fun forRelic(relicId: String): String = "$PREFIX:$relicId"
    }

    /** Panorama hunt hook namespace reserved for relic piece discoveries. */
    object PanoramaHuntKeys {
        const val PREFIX = "panorama_hunt_relic_piece"

        fun forCategory(category: RelicCategory, pieceNumber: Int): String {
            val huntType = PanoramaHuntType.ALL_ORDERED[
                (pieceNumber - 1) % PanoramaHuntType.ALL_ORDERED.size
            ]
            return "$PREFIX:${category.id.lowercase()}:${huntType.id.lowercase()}"
        }
    }

    /** Story-fragment hook namespace reserved for relic narrative rewards. */
    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_relic"

        fun forCategory(category: RelicCategory): String =
            "${PREFIX}_${category.id.lowercase()}"

        fun forRelic(relicId: String): String = "$PREFIX:$relicId"

        fun completionForRelic(relicId: String): String = "${forRelic(relicId)}:completion"

        fun revealForRelic(relicId: String): String = "${forRelic(relicId)}:reveal"

        fun chapterForRelic(relicId: String, chapterNumber: Int): String =
            "${forRelic(relicId)}:chapter_$chapterNumber"

        fun loreForRelic(relicId: String, loreIndex: Int): String =
            "${forRelic(relicId)}:lore_$loreIndex"
    }

    /**
     * Secret-place linkage for relic categories that map to discovery sites.
     * Returns null when a relic family is not tied to a secret-place category yet.
     */
    object SecretPlaceKeys {
        const val PREFIX = "legendary_relic_secret_place"

        fun forCategory(category: RelicCategory): String? =
            linkedSecretPlaceCategory(category)?.let { placeCategory ->
                "${PREFIX}_${placeCategory.id.lowercase()}"
            }

        fun linkedSecretPlaceCategory(category: RelicCategory): SecretPlaceCategory? = when (category) {
            RelicCategory.EXPLORER -> SecretPlaceCategory.EXPLORER_HIDEOUT
            RelicCategory.ANCIENT_CIVILIZATION -> SecretPlaceCategory.ANCIENT_RELIC_SITE
            RelicCategory.MYTHICAL -> SecretPlaceCategory.MYTHICAL_PLACE
            RelicCategory.ROYAL,
            RelicCategory.HIDDEN,
            -> SecretPlaceCategory.LEGENDARY_SITE
            RelicCategory.STORY -> SecretPlaceCategory.HISTORIC_LANDMARK
            RelicCategory.WARRIOR -> SecretPlaceCategory.MYSTERY_ZONE
            RelicCategory.SEASONAL_EVENT -> null
        }

        fun linkedSecretPlaceCategoryId(category: RelicCategory): String? =
            linkedSecretPlaceCategory(category)?.id
    }

    object CategoryGates {
        fun minExplorerLevel(category: RelicCategory): Int = category.minimumExplorerLevel

        fun isUnlocked(category: RelicCategory, explorerLevel: Int): Boolean =
            explorerLevel >= category.minimumExplorerLevel

        fun supportsAchievements(category: RelicCategory): Boolean = when (category) {
            RelicCategory.HIDDEN -> false
            else -> true
        }

        fun supportsStoryFragments(category: RelicCategory): Boolean = when (category) {
            RelicCategory.WARRIOR -> false
            else -> true
        }

        fun supportsSecretPlaces(category: RelicCategory): Boolean =
            SecretPlaceKeys.linkedSecretPlaceCategory(category) != null
    }

    object RewardScaling {
        fun scaledCredits(category: RelicCategory, baseCredits: Int): Int =
            (baseCredits * category.rarityMultiplier).roundToLong().toInt().coerceAtLeast(0)

        fun scaledXp(category: RelicCategory, baseXp: Long): Long =
            (baseXp * category.rarityMultiplier).roundToLong().coerceAtLeast(0L)
    }

    object SourceDomains {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val SECRET_PLACE = "secretPlace"
        const val SEASONAL_EVENT = "seasonalEvent"
        const val PANORAMA = "panorama"
        const val RADAR = "radar"
        const val TREASURE_CLUSTER = "treasureCluster"
        const val DIRECT_DISCOVERY = "directDiscovery"
    }

    /**
     * Hook keys consumed by [com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceEngine]
     * to match domain events against registered piece definitions.
     */
    object PieceSourceKeys {
        const val PREFIX = "relic_piece_source"

        object SecretPlace {
            fun forCategory(category: SecretPlaceCategory): String =
                "${PREFIX}:secret_place:${category.id.lowercase()}"
        }

        object TreasureCluster {
            fun forType(type: ClusterType): String =
                "${PREFIX}:treasure_cluster:${type.id.lowercase()}"

            fun forRelicCategory(category: RelicCategory, pieceNumber: Int): String {
                val clusterType = ClusterType.ALL_ORDERED[
                    (pieceNumber - 1) % ClusterType.ALL_ORDERED.size
                ]
                return forType(clusterType)
            }
        }

        object StoryFragment {
            fun forKey(fragmentKey: String): String =
                "${PREFIX}:story_fragment:$fragmentKey"

            fun forCategory(category: RelicCategory): String =
                forKey(StoryFragmentKeys.forCategory(category))
        }

        object PanoramaHunt {
            fun forType(type: PanoramaHuntType): String =
                "${PREFIX}:panorama_hunt:${type.id.lowercase()}"

            fun forCategory(category: RelicCategory, pieceNumber: Int): String =
                PanoramaHuntKeys.forCategory(category, pieceNumber)
        }

        object SeasonalEvent {
            fun forType(type: SeasonalEventType): String =
                "${PREFIX}:seasonal_event:${type.id.lowercase()}"

            fun forRelicCategory(category: RelicCategory, pieceNumber: Int): String {
                val eventType = SeasonalEventType.ALL_ORDERED[
                    (pieceNumber - 1) % SeasonalEventType.ALL_ORDERED.size
                ]
                return forType(eventType)
            }
        }

        object Achievement {
            fun forKey(achievementKey: String): String =
                "${PREFIX}:achievement:$achievementKey"

            fun forCategory(category: RelicCategory): String =
                forKey(AchievementKeys.firstDiscovery(category))
        }

        object RadarDiscovery {
            fun forType(type: RadarType): String =
                "${PREFIX}:radar:${type.id.lowercase()}"

            fun forRelicCategory(category: RelicCategory, pieceNumber: Int): String {
                val radarType = RadarType.ALL_ORDERED[
                    (pieceNumber - 1) % RadarType.ALL_ORDERED.size
                ]
                return forType(radarType)
            }
        }
    }
}
