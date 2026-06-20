package com.example.roadguideapp.goldhunt.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private fun columnExists(
    db: SupportSQLiteDatabase,
    table: String,
    column: String,
): Boolean {
    db.query("PRAGMA table_info(`$table`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0 && cursor.getString(nameIndex) == column) {
                return true
            }
        }
    }
    return false
}

private fun createCollectedTreasureTable(db: SupportSQLiteDatabase) {
    db.execSQL("DROP INDEX IF EXISTS index_collected_treasure_collectedAt")
    db.execSQL("DROP TABLE IF EXISTS collected_treasure")
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS collected_treasure (
            treasureId TEXT NOT NULL PRIMARY KEY,
            type TEXT NOT NULL,
            lat REAL NOT NULL,
            lng REAL NOT NULL,
            creditAmount INTEGER NOT NULL,
            placementKind TEXT NOT NULL,
            regionL1Id TEXT,
            collectedAt INTEGER NOT NULL,
            generatorVersion INTEGER NOT NULL
        )
        """.trimIndent(),
    )
}

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!columnExists(db, "player_stats", "roadsDiscoveredCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN roadsDiscoveredCount INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "streetsDiscoveredCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN streetsDiscoveredCount INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "regionsCompletedCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN regionsCompletedCount INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "creditsToday")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN creditsToday INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "creditsTodayDate")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN creditsTodayDate TEXT NOT NULL DEFAULT ''",
            )
        }
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS discovered_road (
                roadKey TEXT NOT NULL PRIMARY KEY,
                source TEXT NOT NULL,
                graphImportId TEXT,
                closestNode INTEGER,
                streetGroupKey TEXT,
                regionL1Id TEXT,
                regionL2Id TEXT,
                firstDiscoveredAt INTEGER NOT NULL,
                lastVisitedAt INTEGER NOT NULL,
                firstLat REAL NOT NULL,
                firstLng REAL NOT NULL,
                endLat REAL NOT NULL,
                endLng REAL NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_discovered_road_regionL1Id ON discovered_road(regionL1Id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_discovered_road_streetGroupKey ON discovered_road(streetGroupKey)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS discovered_street (
                streetGroupKey TEXT NOT NULL PRIMARY KEY,
                firstDiscoveredAt INTEGER NOT NULL,
                regionL1Id TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS region_progress (
                regionId TEXT NOT NULL PRIMARY KEY,
                level INTEGER NOT NULL,
                discoveredRoads INTEGER NOT NULL,
                exploredCells INTEGER NOT NULL,
                completionPercent REAL NOT NULL,
                rewardGranted INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS discovery_milestone (
                milestoneId TEXT NOT NULL PRIMARY KEY,
                unlockedAt INTEGER NOT NULL,
                creditAmount INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        if (!columnExists(db, "player_stats", "treasuresCollectedCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN treasuresCollectedCount INTEGER NOT NULL DEFAULT 0",
            )
        }
        createCollectedTreasureTable(db)
    }
}

/** Repair DBs that picked up the invalid index from early Phase 3 builds. */
internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS index_collected_treasure_collectedAt")
    }
}

internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS secret_place_discovery (
                secretPlaceId TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                rarity TEXT NOT NULL,
                lat REAL NOT NULL,
                lng REAL NOT NULL,
                regionL1Id TEXT NOT NULL,
                regionL2Id TEXT NOT NULL,
                displayName TEXT NOT NULL,
                creditsGranted INTEGER NOT NULL,
                treasureNestSlots INTEGER NOT NULL,
                storyFragmentId TEXT,
                placementKind TEXT NOT NULL,
                discoveredAt INTEGER NOT NULL,
                generatorVersion INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_secret_place_discovery_regionL1Id " +
                "ON secret_place_discovery(regionL1Id)",
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS secret_place_region_progress (
                regionL1Id TEXT NOT NULL PRIMARY KEY,
                secretsDiscovered INTEGER NOT NULL,
                rewardGranted INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        if (!columnExists(db, "player_stats", "secretPlacesFoundCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN secretPlacesFoundCount INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "secretPlaceCreditsEarned")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN secretPlaceCreditsEarned INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "rarestSecretRarity")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN rarestSecretRarity INTEGER NOT NULL DEFAULT -1",
            )
        }
        if (!columnExists(db, "player_stats", "secretDiscoveryStreak")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN secretDiscoveryStreak INTEGER NOT NULL DEFAULT 0",
            )
        }
        if (!columnExists(db, "player_stats", "secretStreakDate")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN secretStreakDate TEXT NOT NULL DEFAULT ''",
            )
        }
    }
}

internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS explorer_profile (
                id INTEGER NOT NULL PRIMARY KEY,
                explorerLevel INTEGER NOT NULL DEFAULT 1,
                currentXp INTEGER NOT NULL DEFAULT 0,
                totalCredits INTEGER NOT NULL DEFAULT 0,
                totalRoadsDiscovered INTEGER NOT NULL DEFAULT 0,
                totalAreasDiscovered INTEGER NOT NULL DEFAULT 0,
                totalTreasuresCollected INTEGER NOT NULL DEFAULT 0,
                totalSecretPlacesFound INTEGER NOT NULL DEFAULT 0,
                totalDistanceExploredM REAL NOT NULL DEFAULT 0.0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                createdAtMs INTEGER NOT NULL DEFAULT 0,
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO explorer_profile (
                id,
                explorerLevel,
                currentXp,
                totalCredits,
                totalRoadsDiscovered,
                totalAreasDiscovered,
                totalTreasuresCollected,
                totalSecretPlacesFound,
                totalDistanceExploredM,
                schemaVersion,
                extensionJson,
                createdAtMs,
                updatedAtMs
            )
            SELECT
                1,
                1,
                0,
                COALESCE(totalCredits, 0),
                COALESCE(roadsDiscoveredCount, 0),
                COALESCE(streetsDiscoveredCount, 0),
                COALESCE(treasuresCollectedCount, 0),
                COALESCE(secretPlacesFoundCount, 0),
                COALESCE(totalDistanceM, 0.0),
                1,
                '{}',
                COALESCE(updatedAt, 0),
                COALESCE(updatedAt, 0)
            FROM player_stats
            WHERE id = 1
            """.trimIndent(),
        )
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT OR IGNORE INTO explorer_profile (
                id,
                explorerLevel,
                currentXp,
                schemaVersion,
                extensionJson,
                createdAtMs,
                updatedAtMs
            )
            VALUES (1, 1, 0, 1, '{}', $now, $now)
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS treasure_encyclopedia_entry (
                catalogKey TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL,
                firstDiscoveredAtMs INTEGER,
                bestRarityId TEXT,
                totalCreditsEarned INTEGER NOT NULL DEFAULT 0,
                collectionCount INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        seedEncyclopediaEntries(db)
        backfillEncyclopediaFromCollectedTreasure(db)
    }

    private fun seedEncyclopediaEntries(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            "GOLD_STAR" to "MISSING",
            "FLOWER" to "MISSING",
            "CRYSTAL" to "MISSING",
            "GIFT_BOX" to "MISSING",
            "LEGENDARY_RELIC" to "LOCKED",
            "STORY_FRAGMENT" to "LOCKED",
            "SECRET_PLACE_REWARD" to "LOCKED",
        )
        for ((key, status) in seeds) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO treasure_encyclopedia_entry (
                    catalogKey, status, schemaVersion, extensionJson, updatedAtMs
                ) VALUES ('$key', '$status', 1, '{}', $now)
                """.trimIndent(),
            )
        }
    }

    private fun backfillEncyclopediaFromCollectedTreasure(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "collected_treasure")) return
        val now = System.currentTimeMillis()
        val typeToKey = mapOf(
            "STAR" to "GOLD_STAR",
            "FLOWER" to "FLOWER",
            "CRYSTAL" to "CRYSTAL",
            "GIFT" to "GIFT_BOX",
        )
        for ((type, catalogKey) in typeToKey) {
            db.execSQL(
                """
                INSERT OR REPLACE INTO treasure_encyclopedia_entry (
                    catalogKey,
                    status,
                    firstDiscoveredAtMs,
                    totalCreditsEarned,
                    collectionCount,
                    schemaVersion,
                    extensionJson,
                    updatedAtMs
                )
                SELECT
                    '$catalogKey',
                    'FOUND',
                    MIN(collectedAt),
                    COALESCE(SUM(creditAmount), 0),
                    COUNT(*),
                    1,
                    '{}',
                    $now
                FROM collected_treasure
                WHERE type = '$type'
                HAVING COUNT(*) > 0
                """.trimIndent(),
            )
        }
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS treasure_cluster_encyclopedia_entry (
                catalogKey TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL,
                firstDiscoveredAtMs INTEGER,
                completedAtMs INTEGER,
                totalRewardsEarned INTEGER NOT NULL DEFAULT 0,
                discoveredCount INTEGER NOT NULL DEFAULT 0,
                completedCount INTEGER NOT NULL DEFAULT 0,
                achievementKey TEXT,
                storyFragmentId TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        seedClusterEncyclopediaEntries(db)
        backfillClusterEncyclopediaFromTables(db)
    }

    private fun seedClusterEncyclopediaEntries(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            Triple("ROADSIDE_CACHE", null, null),
            Triple("EXPLORER_NEST", null, null),
            Triple("TREASURE_GARDEN", null, null),
            Triple("ANCIENT_VAULT", "cluster_ancient_vault", "story_fragment_ancient_vault"),
            Triple("LEGENDARY_HOARD", "cluster_legendary_hoard", "story_fragment_legendary_hoard"),
        )
        for ((key, achievement, story) in seeds) {
            val achievementSql = achievement?.let { "'$it'" } ?: "NULL"
            val storySql = story?.let { "'$it'" } ?: "NULL"
            db.execSQL(
                """
                INSERT OR IGNORE INTO treasure_cluster_encyclopedia_entry (
                    catalogKey, status, achievementKey, storyFragmentId,
                    schemaVersion, extensionJson, updatedAtMs
                ) VALUES ('$key', 'MISSING', $achievementSql, $storySql, 1, '{}', $now)
                """.trimIndent(),
            )
        }
    }

    private fun backfillClusterEncyclopediaFromTables(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "treasure_cluster_discovery") &&
            !tableExists(db, "treasure_cluster_completion")
        ) {
            return
        }
        val types = listOf(
            "ROADSIDE_CACHE",
            "EXPLORER_NEST",
            "TREASURE_GARDEN",
            "ANCIENT_VAULT",
            "LEGENDARY_HOARD",
        )
        val now = System.currentTimeMillis()
        for (type in types) {
            db.execSQL(
                """
                INSERT OR REPLACE INTO treasure_cluster_encyclopedia_entry (
                    catalogKey,
                    status,
                    firstDiscoveredAtMs,
                    completedAtMs,
                    totalRewardsEarned,
                    discoveredCount,
                    completedCount,
                    achievementKey,
                    storyFragmentId,
                    schemaVersion,
                    extensionJson,
                    updatedAtMs
                )
                SELECT
                    '$type',
                    CASE
                        WHEN (SELECT COUNT(*) FROM treasure_cluster_discovery WHERE clusterType = '$type') = 0
                            THEN 'MISSING'
                        WHEN (SELECT COUNT(*) FROM treasure_cluster_completion WHERE clusterType = '$type') > 0
                            THEN 'COMPLETED'
                        ELSE 'DISCOVERED'
                    END,
                    (SELECT MIN(discoveredAtMs) FROM treasure_cluster_discovery WHERE clusterType = '$type'),
                    (SELECT MIN(completedAtMs) FROM treasure_cluster_completion WHERE clusterType = '$type'),
                    (SELECT COALESCE(SUM(creditsGranted), 0) FROM treasure_cluster_completion WHERE clusterType = '$type'),
                    (SELECT COUNT(*) FROM treasure_cluster_discovery WHERE clusterType = '$type'),
                    (SELECT COUNT(*) FROM treasure_cluster_completion WHERE clusterType = '$type'),
                    (SELECT achievementKey FROM treasure_cluster_completion WHERE clusterType = '$type' ORDER BY completedAtMs ASC LIMIT 1),
                    (SELECT storyFragmentId FROM treasure_cluster_completion WHERE clusterType = '$type' ORDER BY completedAtMs ASC LIMIT 1),
                    1,
                    '{}',
                    $now
                """.trimIndent(),
            )
        }
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_achievement_progress (
                achievementKey TEXT NOT NULL PRIMARY KEY,
                completionCount INTEGER NOT NULL,
                updatedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_reward_hook (
                eventId TEXT NOT NULL PRIMARY KEY,
                huntId TEXT NOT NULL,
                hookType TEXT NOT NULL,
                hookKey TEXT NOT NULL,
                grantedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS radar_type_cooldown_state (
                radarTypeId TEXT NOT NULL PRIMARY KEY,
                lastScanTimeMs INTEGER NOT NULL DEFAULT 0,
                scanCount INTEGER NOT NULL DEFAULT 0,
                successfulScanCount INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        val now = System.currentTimeMillis()
        val radarTypes = listOf(
            "BASIC_RADAR",
            "TREASURE_RADAR",
            "SECRET_PLACE_RADAR",
            "CLUSTER_RADAR",
            "STORY_RADAR",
            "LEGENDARY_RADAR",
        )
        for (typeId in radarTypes) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO radar_type_cooldown_state (
                    radarTypeId, lastScanTimeMs, scanCount, successfulScanCount,
                    schemaVersion, updatedAtMs
                ) VALUES ('$typeId', 0, 0, 0, 1, $now)
                """.trimIndent(),
            )
        }
        db.execSQL(
            """
            UPDATE radar_type_cooldown_state
            SET
                lastScanTimeMs = (
                    SELECT lastScanTimeMs FROM radar_profile WHERE id = 1
                ),
                scanCount = (
                    SELECT totalScans FROM radar_profile WHERE id = 1
                ),
                successfulScanCount = (
                    SELECT successfulScans FROM radar_profile WHERE id = 1
                ),
                updatedAtMs = $now
            WHERE radarTypeId = (
                SELECT currentRadarTypeId FROM radar_profile WHERE id = 1
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsTotalScans INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsSuccessfulScans INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsClustersFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsSecretPlacesFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsStoryDiscoveries INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN radarStatsLegendaryDiscoveries INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            UPDATE explorer_profile
            SET
                radarStatsTotalScans = (
                    SELECT totalScans FROM radar_profile WHERE id = 1
                ),
                radarStatsSuccessfulScans = (
                    SELECT successfulScans FROM radar_profile WHERE id = 1
                ),
                radarStatsTreasuresFound = (
                    SELECT treasureDetectionsRewarded FROM radar_reward_statistics WHERE id = 1
                ),
                radarStatsClustersFound = (
                    SELECT clusterDetectionsRewarded FROM radar_reward_statistics WHERE id = 1
                ),
                radarStatsSecretPlacesFound = (
                    SELECT secretPlaceDetectionsRewarded FROM radar_reward_statistics WHERE id = 1
                ),
                radarStatsStoryDiscoveries = (
                    SELECT storyFragmentDetectionsRewarded FROM radar_reward_statistics WHERE id = 1
                ),
                radarStatsLegendaryDiscoveries = (
                    SELECT legendaryRelicDetectionsRewarded FROM radar_reward_statistics WHERE id = 1
                )
            WHERE id = 1
            """.trimIndent(),
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 8 WHERE id = 1")
    }
}

internal val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS hidden_relic_discovery (
                relicId TEXT NOT NULL PRIMARY KEY,
                visibility TEXT NOT NULL,
                revealedAtMs INTEGER,
                firstPieceId TEXT,
                storyFragmentKey TEXT,
                storyFragmentRecorded INTEGER NOT NULL DEFAULT 0,
                achievementKey TEXT,
                achievementRecorded INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS hidden_relic_discovery_hook (
                eventId TEXT NOT NULL PRIMARY KEY,
                relicId TEXT NOT NULL,
                hookType TEXT NOT NULL,
                hookKey TEXT NOT NULL,
                grantedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsHiddenCatalogCount INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsHiddenRevealed INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsHiddenUndiscovered INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 14 WHERE id = 1")
    }
}

internal val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS legendary_relic_reward_grant (
                relicId TEXT NOT NULL PRIMARY KEY,
                creditsGranted INTEGER NOT NULL DEFAULT 0,
                xpGranted INTEGER NOT NULL DEFAULT 0,
                storyFragmentKey TEXT,
                storyFragmentRecorded INTEGER NOT NULL DEFAULT 0,
                titleKey TEXT,
                titleRecorded INTEGER NOT NULL DEFAULT 0,
                badgeKey TEXT,
                badgeRecorded INTEGER NOT NULL DEFAULT 0,
                cosmeticKey TEXT,
                cosmeticRecorded INTEGER NOT NULL DEFAULT 0,
                grantedAtMs INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS legendary_relic_reward_hook (
                eventId TEXT NOT NULL PRIMARY KEY,
                relicId TEXT NOT NULL,
                hookType TEXT NOT NULL,
                hookKey TEXT NOT NULL,
                grantedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS legendary_relic (
                relicId TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                rarity TEXT NOT NULL,
                pieceCount INTEGER NOT NULL,
                piecesCollected INTEGER NOT NULL DEFAULT 0,
                completed INTEGER NOT NULL DEFAULT 0,
                completionDateMs INTEGER,
                rewardCredits INTEGER NOT NULL DEFAULT 0,
                rewardXp INTEGER NOT NULL DEFAULT 0,
                badgeKey TEXT,
                titleKey TEXT,
                powerKey TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS relic_piece (
                pieceId TEXT NOT NULL PRIMARY KEY,
                relicId TEXT NOT NULL,
                pieceNumber INTEGER NOT NULL,
                totalPieces INTEGER NOT NULL,
                sourceType TEXT NOT NULL,
                sourceKey TEXT NOT NULL,
                discovered INTEGER NOT NULL DEFAULT 0,
                discoveryDateMs INTEGER,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS relic_piece_grant_hook (
                eventId TEXT NOT NULL PRIMARY KEY,
                pieceId TEXT NOT NULL,
                relicId TEXT NOT NULL,
                hookType TEXT NOT NULL DEFAULT 'relicPiece',
                hookKey TEXT NOT NULL,
                grantedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsCatalogCount INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsCompletedCount INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsPiecesCollected INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsTotalPieces INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsEpicCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsLegendaryCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsCreditsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryRelicStatsXpEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 13 WHERE id = 1")
    }
}

internal val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS achievement_title_entry (
                titleKey TEXT NOT NULL PRIMARY KEY,
                achievementId TEXT NOT NULL,
                displayTitle TEXT NOT NULL,
                rarity TEXT NOT NULL,
                unlockDateMs INTEGER,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN activeAchievementTitleKey TEXT",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 12 WHERE id = 1")
    }
}

internal val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS achievement_badge_entry (
                badgeKey TEXT NOT NULL PRIMARY KEY,
                achievementId TEXT NOT NULL,
                title TEXT NOT NULL,
                iconKey TEXT NOT NULL,
                rarity TEXT NOT NULL,
                unlockDateMs INTEGER,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN achievementStatsCompletedCount INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN achievementStatsRareCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN achievementStatsLegendaryCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN achievementStatsCreditsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN achievementStatsXpEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            UPDATE explorer_profile
            SET achievementStatsCompletedCount = (
                SELECT COUNT(*) FROM achievement WHERE completed = 1
            ),
            achievementStatsCreditsEarned = COALESCE((
                SELECT SUM(creditsGranted) FROM achievement_reward_grant
            ), 0),
            achievementStatsXpEarned = COALESCE((
                SELECT SUM(xpGranted) FROM achievement_reward_grant
            ), 0)
            WHERE id = 1
            """.trimIndent(),
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 11 WHERE id = 1")
    }
}

internal val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS achievement_reward_grant (
                achievementId TEXT NOT NULL PRIMARY KEY,
                creditsGranted INTEGER NOT NULL DEFAULT 0,
                xpGranted INTEGER NOT NULL DEFAULT 0,
                storyFragmentKey TEXT,
                storyFragmentRecorded INTEGER NOT NULL DEFAULT 0,
                legendaryRelicKey TEXT,
                legendaryRelicRecorded INTEGER NOT NULL DEFAULT 0,
                titleKey TEXT,
                titleRecorded INTEGER NOT NULL DEFAULT 0,
                badgeKey TEXT,
                badgeRecorded INTEGER NOT NULL DEFAULT 0,
                grantedAtMs INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS achievement_reward_hook (
                eventId TEXT NOT NULL PRIMARY KEY,
                achievementId TEXT NOT NULL,
                hookType TEXT NOT NULL,
                hookKey TEXT NOT NULL,
                grantedAtMs INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS achievement (
                achievementId TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                targetValue INTEGER NOT NULL DEFAULT 1,
                currentValue INTEGER NOT NULL DEFAULT 0,
                completed INTEGER NOT NULL DEFAULT 0,
                completionDateMs INTEGER,
                rewardCredits INTEGER NOT NULL DEFAULT 0,
                rewardXp INTEGER NOT NULL DEFAULT 0,
                legendaryRelicKey TEXT,
                titleKey TEXT,
                badgeKey TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS seasonal_event_collection_book_entry (
                eventId TEXT NOT NULL PRIMARY KEY,
                eventType TEXT NOT NULL,
                cycleYear INTEGER NOT NULL,
                displayName TEXT NOT NULL,
                status TEXT NOT NULL,
                firstParticipatedAtMs INTEGER,
                completedAtMs INTEGER,
                totalCreditsEarned INTEGER NOT NULL DEFAULT 0,
                totalXpEarned INTEGER NOT NULL DEFAULT 0,
                fragmentsEarned INTEGER NOT NULL DEFAULT 0,
                achievementsEarned INTEGER NOT NULL DEFAULT 0,
                participationAchievementKey TEXT,
                completionAchievementKey TEXT,
                masteryAchievementKey TEXT,
                storyFragmentKey TEXT,
                legendaryRelicKey TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO seasonal_event_collection_book_entry (
                eventId, eventType, cycleYear, displayName, status,
                firstParticipatedAtMs, completedAtMs,
                totalCreditsEarned, totalXpEarned, fragmentsEarned,
                participationAchievementKey, completionAchievementKey,
                masteryAchievementKey, storyFragmentKey, legendaryRelicKey,
                updatedAtMs
            )
            SELECT
                p.eventId,
                p.eventType,
                p.cycleYear,
                p.eventType,
                CASE WHEN p.isCompleted = 1 THEN 'completed' ELSE 'participated' END,
                p.updatedAtMs,
                c.completedAtMs,
                p.creditsEarned + COALESCE(c.creditsGranted, 0),
                p.xpEarned + COALESCE(c.xpGranted, 0),
                p.fragmentsEarned,
                NULL, NULL, NULL, NULL, NULL,
                p.updatedAtMs
            FROM seasonal_event_progress p
            LEFT JOIN seasonal_event_achievement_completion c ON c.eventId = p.eventId
            WHERE p.treasuresCollected > 0
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS seasonal_event_achievement_completion (
                eventId TEXT NOT NULL PRIMARY KEY,
                eventType TEXT NOT NULL,
                cycleYear INTEGER NOT NULL,
                achievementKey TEXT NOT NULL,
                creditsGranted INTEGER NOT NULL DEFAULT 0,
                xpGranted INTEGER NOT NULL DEFAULT 0,
                category TEXT,
                completedAtMs INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsEventsJoined INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsRewardsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsBestEventType TEXT",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsFavoriteEventType TEXT",
        )
        db.execSQL(
            """
            UPDATE explorer_profile
            SET seasonalEventStatsEventsJoined = (
                SELECT COUNT(*) FROM seasonal_event_progress WHERE treasuresCollected > 0
            ),
            seasonalEventStatsRewardsEarned = (
                SELECT COALESCE(SUM(treasuresCollected), 0) FROM seasonal_event_progress
            )
            WHERE id = 1
            """.trimIndent(),
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 10 WHERE id = 1")
    }
}

internal val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS seasonal_event_progress (
                eventId TEXT NOT NULL PRIMARY KEY,
                eventType TEXT NOT NULL,
                cycleYear INTEGER NOT NULL,
                treasuresCollected INTEGER NOT NULL DEFAULT 0,
                xpEarned INTEGER NOT NULL DEFAULT 0,
                creditsEarned INTEGER NOT NULL DEFAULT 0,
                fragmentsEarned INTEGER NOT NULL DEFAULT 0,
                completionPercent REAL NOT NULL DEFAULT 0,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsTreasuresCollected INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsXpEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsCreditsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsFragmentsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN seasonalEventStatsCompletedCycles INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 9 WHERE id = 1")
    }
}

internal val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS radar_reward_statistics (
                id INTEGER NOT NULL PRIMARY KEY,
                totalXpEarned INTEGER NOT NULL DEFAULT 0,
                totalCreditsEarned INTEGER NOT NULL DEFAULT 0,
                rewardedScans INTEGER NOT NULL DEFAULT 0,
                rewardedDetections INTEGER NOT NULL DEFAULT 0,
                treasureDetectionsRewarded INTEGER NOT NULL DEFAULT 0,
                clusterDetectionsRewarded INTEGER NOT NULL DEFAULT 0,
                secretPlaceDetectionsRewarded INTEGER NOT NULL DEFAULT 0,
                storyFragmentDetectionsRewarded INTEGER NOT NULL DEFAULT 0,
                legendaryRelicDetectionsRewarded INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT OR IGNORE INTO radar_reward_statistics (
                id, totalXpEarned, totalCreditsEarned, rewardedScans, rewardedDetections,
                treasureDetectionsRewarded, clusterDetectionsRewarded,
                secretPlaceDetectionsRewarded, storyFragmentDetectionsRewarded,
                legendaryRelicDetectionsRewarded, schemaVersion, updatedAtMs
            ) VALUES (1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, $now)
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS radar_profile (
                id INTEGER NOT NULL PRIMARY KEY,
                currentRadarTypeId TEXT NOT NULL DEFAULT 'BASIC_RADAR',
                unlockLevel INTEGER NOT NULL DEFAULT 1,
                lastScanTimeMs INTEGER NOT NULL DEFAULT 0,
                totalScans INTEGER NOT NULL DEFAULT 0,
                successfulScans INTEGER NOT NULL DEFAULT 0,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                createdAtMs INTEGER NOT NULL DEFAULT 0,
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT OR IGNORE INTO radar_profile (
                id, currentRadarTypeId, unlockLevel, lastScanTimeMs, totalScans, successfulScans,
                schemaVersion, extensionJson, createdAtMs, updatedAtMs
            ) VALUES (1, 'BASIC_RADAR', 1, 0, 0, 0, 1, '{}', $now, $now)
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_journal_entry (
                catalogKey TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL,
                firstDiscoveredAtMs INTEGER,
                completedAtMs INTEGER,
                totalRewardsEarned INTEGER NOT NULL DEFAULT 0,
                totalXpEarned INTEGER NOT NULL DEFAULT 0,
                discoveredCount INTEGER NOT NULL DEFAULT 0,
                completedCount INTEGER NOT NULL DEFAULT 0,
                achievementKey TEXT,
                storyFragmentId TEXT,
                legendaryRelicKey TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        seedPanoramaHuntJournalEntries(db)
        backfillPanoramaHuntJournalFromTables(db)
    }

    private fun seedPanoramaHuntJournalEntries(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            listOf("HIDDEN_SYMBOL", "panorama_hunt_hidden_symbol", null, null),
            listOf("OBJECT_HUNT", "panorama_hunt_object_hunt", null, null),
            listOf("PANORAMA_PUZZLE", "panorama_hunt_panorama_puzzle", "story_fragment_panorama_puzzle", null),
            listOf("SECRET_CODE", "panorama_hunt_secret_code", "story_fragment_secret_code", null),
            listOf("RELIC_HUNT", "panorama_hunt_relic_hunt", "story_fragment_relic_hunt", "legendary_relic_panorama_hunt"),
        )
        for (seed in seeds) {
            val achievementSql = seed[1]?.let { "'$it'" } ?: "NULL"
            val storySql = seed[2]?.let { "'$it'" } ?: "NULL"
            val relicSql = seed[3]?.let { "'$it'" } ?: "NULL"
            db.execSQL(
                """
                INSERT OR IGNORE INTO panorama_hunt_journal_entry (
                    catalogKey, status, achievementKey, storyFragmentId, legendaryRelicKey,
                    schemaVersion, extensionJson, updatedAtMs
                ) VALUES ('${seed[0]}', 'MISSING', $achievementSql, $storySql, $relicSql, 1, '{}', $now)
                """.trimIndent(),
            )
        }
    }

    private fun backfillPanoramaHuntJournalFromTables(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "panorama_hunt_session") &&
            !tableExists(db, "panorama_hunt_completion")
        ) {
            return
        }
        val types = listOf(
            "HIDDEN_SYMBOL",
            "OBJECT_HUNT",
            "PANORAMA_PUZZLE",
            "SECRET_CODE",
            "RELIC_HUNT",
        )
        val now = System.currentTimeMillis()
        for (type in types) {
            db.execSQL(
                """
                INSERT OR REPLACE INTO panorama_hunt_journal_entry (
                    catalogKey,
                    status,
                    firstDiscoveredAtMs,
                    completedAtMs,
                    totalRewardsEarned,
                    totalXpEarned,
                    discoveredCount,
                    completedCount,
                    achievementKey,
                    storyFragmentId,
                    legendaryRelicKey,
                    schemaVersion,
                    extensionJson,
                    updatedAtMs
                )
                SELECT
                    '$type',
                    CASE
                        WHEN (SELECT COUNT(*) FROM panorama_hunt_session WHERE huntType = '$type') = 0
                            THEN 'MISSING'
                        WHEN (SELECT COUNT(*) FROM panorama_hunt_completion WHERE huntType = '$type') > 0
                            THEN 'COMPLETED'
                        ELSE 'DISCOVERED'
                    END,
                    (SELECT MIN(startedAtMs) FROM panorama_hunt_session WHERE huntType = '$type'),
                    (SELECT MAX(completedAtMs) FROM panorama_hunt_completion WHERE huntType = '$type'),
                    (SELECT COALESCE(SUM(creditsGranted), 0) FROM panorama_hunt_completion WHERE huntType = '$type'),
                    (SELECT COALESCE(SUM(xpGranted), 0) FROM panorama_hunt_completion WHERE huntType = '$type'),
                    (SELECT COUNT(*) FROM panorama_hunt_session WHERE huntType = '$type'),
                    (SELECT COUNT(*) FROM panorama_hunt_completion WHERE huntType = '$type'),
                    (SELECT achievementKey FROM panorama_hunt_completion WHERE huntType = '$type' ORDER BY completedAtMs DESC LIMIT 1),
                    (SELECT storyFragmentId FROM panorama_hunt_completion WHERE huntType = '$type' ORDER BY completedAtMs DESC LIMIT 1),
                    (SELECT legendaryRelicKey FROM panorama_hunt_completion WHERE huntType = '$type' ORDER BY completedAtMs DESC LIMIT 1),
                    1,
                    '{}',
                    $now
                """.trimIndent(),
            )
        }
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_session (
                huntId TEXT NOT NULL PRIMARY KEY,
                huntType TEXT NOT NULL,
                secretPlaceId TEXT NOT NULL,
                panoramaId TEXT NOT NULL,
                startedAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsTotalHunts INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO panorama_hunt_session (
                huntId, huntType, secretPlaceId, panoramaId, startedAtMs, schemaVersion
            )
            SELECT
                huntId,
                huntType,
                secretPlaceId,
                panoramaId,
                completedAtMs,
                1
            FROM panorama_hunt_completion
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO panorama_hunt_session (
                huntId, huntType, secretPlaceId, panoramaId, startedAtMs, schemaVersion
            )
            SELECT
                huntId,
                'UNKNOWN',
                '',
                '',
                MIN(foundAtMs),
                1
            FROM panorama_hunt_target_finding
            GROUP BY huntId
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE explorer_profile
            SET panoramaHuntStatsTotalHunts = (
                SELECT COUNT(*) FROM panorama_hunt_session
            )
            WHERE id = 1
            """.trimIndent(),
        )
    }
}

internal val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_target_finding (
                targetId TEXT NOT NULL PRIMARY KEY,
                huntId TEXT NOT NULL,
                slotIndex INTEGER NOT NULL,
                foundAtMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS panorama_hunt_completion (
                huntId TEXT NOT NULL PRIMARY KEY,
                huntType TEXT NOT NULL,
                secretPlaceId TEXT NOT NULL,
                panoramaId TEXT NOT NULL,
                targetsFound INTEGER NOT NULL,
                targetsTotal INTEGER NOT NULL,
                creditsGranted INTEGER NOT NULL,
                xpGranted INTEGER NOT NULL,
                completedAtMs INTEGER NOT NULL,
                achievementKey TEXT,
                storyFragmentId TEXT,
                legendaryRelicKey TEXT,
                rewardEventId TEXT NOT NULL,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN totalPanoramaHuntsCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsTargetsFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsCreditsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsXpEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsHiddenSymbolCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsObjectHuntCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsPanoramaPuzzleCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsSecretCodeCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN panoramaHuntStatsRelicHuntCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 7 WHERE id = 1")
    }
}

internal val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS secret_place_collection_book_entry (
                catalogKey TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL,
                firstDiscoveredAtMs INTEGER,
                completedAtMs INTEGER,
                totalCreditsEarned INTEGER NOT NULL DEFAULT 0,
                totalXpEarned INTEGER NOT NULL DEFAULT 0,
                discoveredCount INTEGER NOT NULL DEFAULT 0,
                completedCount INTEGER NOT NULL DEFAULT 0,
                minimumExplorerLevel INTEGER NOT NULL DEFAULT 1,
                achievementKey TEXT,
                storyFragmentId TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                updatedAtMs INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        seedSecretPlaceCollectionBookEntries(db)
        backfillSecretPlaceCollectionBookFromProfile(db)
    }

    private fun seedSecretPlaceCollectionBookEntries(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            listOf("NATURE_SANCTUARY", null, null, "1"),
            listOf("SCENIC_VIEWPOINT", null, "panorama_hunt_scenic_viewpoint", "2"),
            listOf("HISTORIC_LANDMARK", "secret_place_historic_landmark", "story_fragment_historic_landmark", "3"),
            listOf("MYSTERY_ZONE", "secret_place_mystery_zone", null, "4"),
            listOf("ANCIENT_RELIC_SITE", "secret_place_ancient_relic_site", "story_fragment_ancient_relic_site", "5"),
            listOf("EXPLORER_HIDEOUT", "secret_place_explorer_hideout", null, "6"),
            listOf("MYTHICAL_PLACE", "secret_place_mythical_place", "story_fragment_mythical_place", "7"),
            listOf("LEGENDARY_SITE", "secret_place_legendary_site", "story_fragment_legendary_site", "8"),
        )
        for (seed in seeds) {
            val key = seed[0]!!
            val achievement = seed[1]
            val story = seed[2]
            val level = seed[3]!!
            val achievementSql = achievement?.let { "'$it'" } ?: "NULL"
            val storySql = story?.let { "'$it'" } ?: "NULL"
            db.execSQL(
                """
                INSERT OR IGNORE INTO secret_place_collection_book_entry (
                    catalogKey, status, minimumExplorerLevel, achievementKey, storyFragmentId,
                    schemaVersion, extensionJson, updatedAtMs
                ) VALUES ('$key', 'MISSING', $level, $achievementSql, $storySql, 1, '{}', $now)
                """.trimIndent(),
            )
        }
    }

    private fun backfillSecretPlaceCollectionBookFromProfile(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "explorer_profile")) return
        val categories = listOf(
            "NATURE_SANCTUARY" to Pair("secretPlaceCatStatsNatureSanctuaryFound", "secretPlaceCatStatsNatureSanctuaryCompleted"),
            "SCENIC_VIEWPOINT" to Pair("secretPlaceCatStatsScenicViewpointFound", "secretPlaceCatStatsScenicViewpointCompleted"),
            "HISTORIC_LANDMARK" to Pair("secretPlaceCatStatsHistoricLandmarkFound", "secretPlaceCatStatsHistoricLandmarkCompleted"),
            "MYSTERY_ZONE" to Pair("secretPlaceCatStatsMysteryZoneFound", "secretPlaceCatStatsMysteryZoneCompleted"),
            "ANCIENT_RELIC_SITE" to Pair("secretPlaceCatStatsAncientRelicSiteFound", "secretPlaceCatStatsAncientRelicSiteCompleted"),
            "EXPLORER_HIDEOUT" to Pair("secretPlaceCatStatsExplorerHideoutFound", "secretPlaceCatStatsExplorerHideoutCompleted"),
            "MYTHICAL_PLACE" to Pair("secretPlaceCatStatsMythicalPlaceFound", "secretPlaceCatStatsMythicalPlaceCompleted"),
            "LEGENDARY_SITE" to Pair("secretPlaceCatStatsLegendarySiteFound", "secretPlaceCatStatsLegendarySiteCompleted"),
        )
        val now = System.currentTimeMillis()
        for ((category, columns) in categories) {
            val (foundCol, completedCol) = columns
            db.execSQL(
                """
                INSERT OR REPLACE INTO secret_place_collection_book_entry (
                    catalogKey,
                    status,
                    discoveredCount,
                    completedCount,
                    minimumExplorerLevel,
                    achievementKey,
                    storyFragmentId,
                    schemaVersion,
                    extensionJson,
                    updatedAtMs
                )
                SELECT
                    '$category',
                    CASE
                        WHEN ep.$foundCol <= 0 THEN 'MISSING'
                        WHEN ep.$completedCol > 0 THEN 'COMPLETED'
                        ELSE 'DISCOVERED'
                    END,
                    ep.$foundCol,
                    ep.$completedCol,
                    cb.minimumExplorerLevel,
                    cb.achievementKey,
                    cb.storyFragmentId,
                    1,
                    '{}',
                    $now
                FROM explorer_profile ep
                LEFT JOIN secret_place_collection_book_entry cb ON cb.catalogKey = '$category'
                WHERE ep.id = 1 AND (ep.$foundCol > 0 OR ep.$completedCol > 0)
                """.trimIndent(),
            )
        }
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsDiscovered INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsCreditsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsXpEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsBestCategoryId TEXT",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsRarestCategoryId TEXT",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsNatureSanctuaryFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsScenicViewpointFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsHistoricLandmarkFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsMysteryZoneFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsAncientRelicSiteFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsExplorerHideoutFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsMythicalPlaceFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsLegendarySiteFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsNatureSanctuaryCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsScenicViewpointCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsHistoricLandmarkCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsMysteryZoneCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsAncientRelicSiteCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsExplorerHideoutCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsMythicalPlaceCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN secretPlaceCatStatsLegendarySiteCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 6 WHERE id = 1")
    }
}

internal val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN totalClusterRewardsEarned INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsRoadsideCachesFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsExplorerNestsFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsTreasureGardensFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsAncientVaultsFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsLegendaryHoardsFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsRoadsideCachesCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsExplorerNestsCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsTreasureGardensCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsAncientVaultsCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN clusterStatsLegendaryHoardsCompleted INTEGER NOT NULL DEFAULT 0",
        )
        backfillClusterStatistics(db)
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 5 WHERE id = 1")
    }

    private fun backfillClusterStatistics(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "treasure_cluster_discovery") &&
            !tableExists(db, "treasure_cluster_completion")
        ) {
            return
        }
        db.execSQL(
            """
            UPDATE explorer_profile SET
                totalClustersDiscovered = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                ),
                totalClustersCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                ),
                totalClusterRewardsEarned = (
                    SELECT COALESCE(SUM(creditsGranted), 0) FROM treasure_cluster_completion
                ),
                clusterStatsRoadsideCachesFound = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                    WHERE clusterType = 'ROADSIDE_CACHE'
                ),
                clusterStatsExplorerNestsFound = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                    WHERE clusterType = 'EXPLORER_NEST'
                ),
                clusterStatsTreasureGardensFound = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                    WHERE clusterType = 'TREASURE_GARDEN'
                ),
                clusterStatsAncientVaultsFound = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                    WHERE clusterType = 'ANCIENT_VAULT'
                ),
                clusterStatsLegendaryHoardsFound = (
                    SELECT COUNT(*) FROM treasure_cluster_discovery
                    WHERE clusterType = 'LEGENDARY_HOARD'
                ),
                clusterStatsRoadsideCachesCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                    WHERE clusterType = 'ROADSIDE_CACHE'
                ),
                clusterStatsExplorerNestsCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                    WHERE clusterType = 'EXPLORER_NEST'
                ),
                clusterStatsTreasureGardensCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                    WHERE clusterType = 'TREASURE_GARDEN'
                ),
                clusterStatsAncientVaultsCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                    WHERE clusterType = 'ANCIENT_VAULT'
                ),
                clusterStatsLegendaryHoardsCompleted = (
                    SELECT COUNT(*) FROM treasure_cluster_completion
                    WHERE clusterType = 'LEGENDARY_HOARD'
                )
            WHERE id = 1
            """.trimIndent(),
        )
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS treasure_cluster_completion (
                clusterId TEXT NOT NULL PRIMARY KEY,
                clusterType TEXT NOT NULL,
                treasureCount INTEGER NOT NULL,
                creditsGranted INTEGER NOT NULL,
                xpGranted INTEGER NOT NULL,
                completedAtMs INTEGER NOT NULL,
                achievementKey TEXT,
                storyFragmentId TEXT,
                legendaryRelicKey TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1,
                extensionJson TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN totalClustersCompleted INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 4 WHERE id = 1")
    }
}

internal val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS treasure_cluster_discovery (
                clusterId TEXT NOT NULL PRIMARY KEY,
                clusterType TEXT NOT NULL,
                centerLatitude REAL NOT NULL,
                centerLongitude REAL NOT NULL,
                radiusMeters REAL NOT NULL,
                discoveredAtMs INTEGER NOT NULL,
                discoveryTrigger TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN totalClustersDiscovered INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 3 WHERE id = 1")
    }
}

internal val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN commonTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN uncommonTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN rareTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN epicTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN legendaryTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE explorer_profile ADD COLUMN mythicTreasuresFound INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL("UPDATE explorer_profile SET schemaVersion = 2 WHERE id = 1")
        backfillTreasureRarityStatsFromCollectedTreasure(db)
    }

    private fun backfillTreasureRarityStatsFromCollectedTreasure(db: SupportSQLiteDatabase) {
        if (!tableExists(db, "collected_treasure")) return
        db.execSQL(
            """
            UPDATE explorer_profile SET
                commonTreasuresFound = (
                    SELECT COUNT(*) FROM collected_treasure
                    WHERE type IN ('STAR', 'HINT')
                ),
                uncommonTreasuresFound = (
                    SELECT COUNT(*) FROM collected_treasure WHERE type = 'FLOWER'
                ),
                rareTreasuresFound = (
                    SELECT COUNT(*) FROM collected_treasure WHERE type = 'CRYSTAL'
                ),
                epicTreasuresFound = (
                    SELECT COUNT(*) FROM collected_treasure WHERE type = 'GIFT'
                )
            WHERE id = 1
            """.trimIndent(),
        )
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}

internal val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS level_up_event (
                id TEXT NOT NULL PRIMARY KEY,
                previousLevel INTEGER NOT NULL,
                newLevel INTEGER NOT NULL,
                timestampMs INTEGER NOT NULL,
                lifetimeXpAtLevelUp INTEGER NOT NULL DEFAULT 0,
                extensionJson TEXT NOT NULL DEFAULT '{}',
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_level_up_event_timestampMs ON level_up_event(timestampMs)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_level_up_event_newLevel ON level_up_event(newLevel)",
        )
    }
}

internal val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS xp_transaction (
                id TEXT NOT NULL PRIMARY KEY,
                timestampMs INTEGER NOT NULL,
                source TEXT NOT NULL,
                xpAwarded INTEGER NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                metadataJson TEXT,
                schemaVersion INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_xp_transaction_timestampMs ON xp_transaction(timestampMs)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_xp_transaction_source ON xp_transaction(source)",
        )
    }
}

/** Rebuild treasure table if a device still has schema drift at v4. */
internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createCollectedTreasureTable(db)
        if (!columnExists(db, "player_stats", "treasuresCollectedCount")) {
            db.execSQL(
                "ALTER TABLE player_stats ADD COLUMN treasuresCollectedCount INTEGER NOT NULL DEFAULT 0",
            )
        }
    }
}
