package com.example.roadguideapp.goldhunt.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
internal interface ExploredCellDao {
    @Query("SELECT cellId FROM explored_cell WHERE cellId = :cellId LIMIT 1")
    suspend fun findId(cellId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell: ExploredCellEntity): Long

    @Query("SELECT COUNT(*) FROM explored_cell WHERE level = 0")
    suspend fun countL0(): Int
}

@Dao
internal interface CreditEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: CreditEventEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM credit_event")
    suspend fun sumCredits(): Int
}

@Dao
internal interface DistrictDiscoveryDao {
    @Query("SELECT districtKey FROM district_discovery WHERE districtKey = :key LIMIT 1")
    suspend fun findKey(key: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: DistrictDiscoveryEntity): Long
}

@Dao
internal interface L1ProgressDao {
    @Query("SELECT * FROM l1_progress WHERE l1CellId = :l1Id LIMIT 1")
    suspend fun find(l1Id: String): L1ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: L1ProgressEntity): Long

    @Update
    suspend fun update(row: L1ProgressEntity)
}

@Dao
internal interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    suspend fun get(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: PlayerStatsEntity)
}

@Dao
internal interface DiscoveredRoadDao {
    @Query("SELECT roadKey FROM discovered_road WHERE roadKey = :roadKey LIMIT 1")
    suspend fun findKey(roadKey: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: DiscoveredRoadEntity): Long
}

@Dao
internal interface DiscoveredStreetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: DiscoveredStreetEntity): Long
}

@Dao
internal interface RegionProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: RegionProgressEntity): Long
}

@Dao
internal interface DiscoveryMilestoneDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: DiscoveryMilestoneEntity): Long
}

@Dao
internal interface CollectedTreasureDao {
    @Query("SELECT treasureId FROM collected_treasure WHERE treasureId = :treasureId LIMIT 1")
    suspend fun findId(treasureId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: CollectedTreasureEntity): Long

    @Query("SELECT COUNT(*) FROM collected_treasure")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM collected_treasure WHERE type = :type")
    suspend fun countByType(type: String): Int

    @Query("SELECT treasureId FROM collected_treasure")
    suspend fun allTreasureIds(): List<String>

    @Query("SELECT * FROM collected_treasure ORDER BY collectedAt ASC")
    suspend fun allOrdered(): List<CollectedTreasureEntity>
}

@Dao
internal interface SecretPlaceDiscoveryDao {
    @Query("SELECT secretPlaceId FROM secret_place_discovery WHERE secretPlaceId = :secretPlaceId LIMIT 1")
    suspend fun findId(secretPlaceId: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: SecretPlaceDiscoveryEntity): Long

    @Query("SELECT COUNT(*) FROM secret_place_discovery")
    suspend fun count(): Int
}

@Dao
internal interface SecretPlaceRegionProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: SecretPlaceRegionProgressEntity): Long
}
