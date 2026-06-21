package com.example.roadguideapp.goldhunt.relics.hidden

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface HiddenRelicDiscoveryDao {
    @Query("SELECT * FROM hidden_relic_discovery ORDER BY relicId ASC")
    suspend fun getAll(): List<HiddenRelicDiscoveryEntity>

    @Query("SELECT * FROM hidden_relic_discovery WHERE relicId = :relicId LIMIT 1")
    suspend fun get(relicId: String): HiddenRelicDiscoveryEntity?

    @Query("SELECT COUNT(*) FROM hidden_relic_discovery WHERE visibility = :visibility")
    suspend fun countByVisibility(visibility: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM hidden_relic_discovery
        WHERE visibility = :visibility AND revealedAtMs IS NOT NULL AND revealedAtMs > 0
        """,
    )
    suspend fun countRevealedByVisibility(visibility: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HiddenRelicDiscoveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HiddenRelicDiscoveryEntity>)
}
