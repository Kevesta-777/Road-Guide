package com.example.roadguideapp.goldhunt.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileDao
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.profile.levelup.LevelUpEventDao
import com.example.roadguideapp.goldhunt.profile.levelup.LevelUpEventEntity
import com.example.roadguideapp.goldhunt.profile.xp.XpTransactionDao
import com.example.roadguideapp.goldhunt.profile.xp.XpTransactionEntity
import com.example.roadguideapp.goldhunt.clusters.completion.TreasureClusterCompletionDao
import com.example.roadguideapp.goldhunt.clusters.completion.TreasureClusterCompletionEntity
import com.example.roadguideapp.goldhunt.clusters.discovery.TreasureClusterDiscoveryDao
import com.example.roadguideapp.goldhunt.clusters.discovery.TreasureClusterDiscoveryEntity
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaDao
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaEntryEntity
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntCompletionDao
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntCompletionEntity
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntTargetFindingDao
import com.example.roadguideapp.goldhunt.panorama.completion.PanoramaHuntTargetFindingEntity
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressEntity
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressDao
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardHookDao
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardHookEntity
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalDao
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalEntryEntity
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntSessionDao
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntSessionEntity
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookDao
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookEntryEntity
import com.example.roadguideapp.goldhunt.radar.RadarProfileDao
import com.example.roadguideapp.goldhunt.radar.RadarProfileEntity
import com.example.roadguideapp.goldhunt.radar.RadarTypeCooldownStateDao
import com.example.roadguideapp.goldhunt.radar.RadarTypeCooldownStateEntity
import com.example.roadguideapp.goldhunt.radar.rewards.RadarRewardStatisticsDao
import com.example.roadguideapp.goldhunt.radar.rewards.RadarRewardStatisticsEntity
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaDao
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCompletionDao
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCompletionEntity
import com.example.roadguideapp.goldhunt.achievements.AchievementDao
import com.example.roadguideapp.goldhunt.achievements.AchievementEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantEntity
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeDao
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntryEntity
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDao
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntryEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookDao
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookEntryEntity
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressDao
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressEntity
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaEntryEntity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicEntity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDao
import com.example.roadguideapp.goldhunt.relics.RelicPieceEntity
import com.example.roadguideapp.goldhunt.relics.RelicPieceDao
import com.example.roadguideapp.goldhunt.relics.progress.RelicPieceGrantHookEntity
import com.example.roadguideapp.goldhunt.relics.progress.RelicPieceGrantHookDao
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantDao
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookDao
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryEntity
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryDao
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryHookEntity
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryHookDao

@Database(
    entities = [
        ExploredCellEntity::class,
        CreditEventEntity::class,
        DistrictDiscoveryEntity::class,
        PlayerStatsEntity::class,
        L1ProgressEntity::class,
        DiscoveredRoadEntity::class,
        DiscoveredStreetEntity::class,
        RegionProgressEntity::class,
        DiscoveryMilestoneEntity::class,
        CollectedTreasureEntity::class,
        SecretPlaceDiscoveryEntity::class,
        SecretPlaceRegionProgressEntity::class,
        ExplorerProfileEntity::class,
        XpTransactionEntity::class,
        LevelUpEventEntity::class,
        TreasureEncyclopediaEntryEntity::class,
        TreasureClusterDiscoveryEntity::class,
        TreasureClusterCompletionEntity::class,
        ClusterEncyclopediaEntryEntity::class,
        SecretPlaceCollectionBookEntryEntity::class,
        PanoramaHuntTargetFindingEntity::class,
        PanoramaHuntCompletionEntity::class,
        PanoramaHuntAchievementProgressEntity::class,
        PanoramaHuntRewardHookEntity::class,
        PanoramaHuntSessionEntity::class,
        PanoramaHuntJournalEntryEntity::class,
        RadarProfileEntity::class,
        RadarTypeCooldownStateEntity::class,
        RadarRewardStatisticsEntity::class,
        SeasonalEventProgressEntity::class,
        SeasonalEventAchievementCompletionEntity::class,
        SeasonalEventCollectionBookEntryEntity::class,
        AchievementEntity::class,
        AchievementRewardGrantEntity::class,
        AchievementRewardHookEntity::class,
        AchievementBadgeEntryEntity::class,
        AchievementTitleEntryEntity::class,
        LegendaryRelicEntity::class,
        RelicPieceEntity::class,
        RelicPieceGrantHookEntity::class,
        LegendaryRelicRewardGrantEntity::class,
        LegendaryRelicRewardHookEntity::class,
        HiddenRelicDiscoveryEntity::class,
        HiddenRelicDiscoveryHookEntity::class,
    ],
    version = 37,
    exportSchema = false,
)
internal abstract class GoldHuntDatabase : RoomDatabase() {
    abstract fun exploredCellDao(): ExploredCellDao
    abstract fun creditEventDao(): CreditEventDao
    abstract fun districtDiscoveryDao(): DistrictDiscoveryDao
    abstract fun l1ProgressDao(): L1ProgressDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun discoveredRoadDao(): DiscoveredRoadDao
    abstract fun discoveredStreetDao(): DiscoveredStreetDao
    abstract fun regionProgressDao(): RegionProgressDao
    abstract fun discoveryMilestoneDao(): DiscoveryMilestoneDao
    abstract fun collectedTreasureDao(): CollectedTreasureDao
    abstract fun secretPlaceDiscoveryDao(): SecretPlaceDiscoveryDao
    abstract fun secretPlaceRegionProgressDao(): SecretPlaceRegionProgressDao
    abstract fun explorerProfileDao(): ExplorerProfileDao
    abstract fun xpTransactionDao(): XpTransactionDao
    abstract fun levelUpEventDao(): LevelUpEventDao
    abstract fun treasureEncyclopediaDao(): TreasureEncyclopediaDao
    abstract fun treasureClusterDiscoveryDao(): TreasureClusterDiscoveryDao
    abstract fun treasureClusterCompletionDao(): TreasureClusterCompletionDao
    abstract fun clusterEncyclopediaDao(): ClusterEncyclopediaDao
    abstract fun secretPlaceCollectionBookDao(): SecretPlaceCollectionBookDao
    abstract fun panoramaHuntTargetFindingDao(): PanoramaHuntTargetFindingDao
    abstract fun panoramaHuntCompletionDao(): PanoramaHuntCompletionDao
    abstract fun panoramaHuntAchievementProgressDao(): PanoramaHuntAchievementProgressDao
    abstract fun panoramaHuntRewardHookDao(): PanoramaHuntRewardHookDao
    abstract fun panoramaHuntSessionDao(): PanoramaHuntSessionDao
    abstract fun panoramaHuntJournalDao(): PanoramaHuntJournalDao
    abstract fun radarProfileDao(): RadarProfileDao
    abstract fun radarTypeCooldownStateDao(): RadarTypeCooldownStateDao
    abstract fun radarRewardStatisticsDao(): RadarRewardStatisticsDao
    abstract fun seasonalEventProgressDao(): SeasonalEventProgressDao
    abstract fun seasonalEventAchievementCompletionDao(): SeasonalEventAchievementCompletionDao
    abstract fun seasonalEventCollectionBookDao(): SeasonalEventCollectionBookDao
    abstract fun achievementDao(): AchievementDao
    abstract fun achievementRewardGrantDao(): AchievementRewardGrantDao
    abstract fun achievementRewardHookDao(): AchievementRewardHookDao
    abstract fun achievementBadgeDao(): AchievementBadgeDao
    abstract fun achievementTitleDao(): AchievementTitleDao
    abstract fun legendaryRelicDao(): LegendaryRelicDao
    abstract fun relicPieceDao(): RelicPieceDao
    abstract fun relicPieceGrantHookDao(): RelicPieceGrantHookDao
    abstract fun legendaryRelicRewardGrantDao(): LegendaryRelicRewardGrantDao
    abstract fun legendaryRelicRewardHookDao(): LegendaryRelicRewardHookDao
    abstract fun hiddenRelicDiscoveryDao(): HiddenRelicDiscoveryDao
    abstract fun hiddenRelicDiscoveryHookDao(): HiddenRelicDiscoveryHookDao

    companion object {
        private const val TAG = "GoldHuntDatabase"
        private const val DB_NAME = "goldhunt.db"

        @Volatile
        private var instance: GoldHuntDatabase? = null

        fun get(context: Context): GoldHuntDatabase {
            instance?.let { return it }
            return synchronized(this) {
                instance ?: openDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun openDatabase(context: Context): GoldHuntDatabase {
            return try {
                buildDatabase(context)
            } catch (first: Exception) {
                Log.e(TAG, "Opening $DB_NAME failed; recreating database", first)
                instance?.close()
                instance = null
                context.deleteDatabase(DB_NAME)
                try {
                    buildDatabase(context)
                } catch (second: Exception) {
                    Log.e(TAG, "Recreating $DB_NAME failed", second)
                    throw second
                }
            }
        }

        private fun buildDatabase(context: Context): GoldHuntDatabase =
            Room.databaseBuilder(context, GoldHuntDatabase::class.java, DB_NAME)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32,
                    MIGRATION_32_33,
                    MIGRATION_33_34,
                    MIGRATION_34_35,
                    MIGRATION_35_36,
                    MIGRATION_36_37,
                )
                .build()
    }
}
