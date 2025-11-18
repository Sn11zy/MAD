package com.example.sportsorganizer.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportsorganizer.data.local.entities.CompetitionConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: CompetitionConfig): Long

    @Query("SELECT * FROM competition_config WHERE competition_id = :competitionId LIMIT 1")
    fun getByCompetitionIdFlow(competitionId: Long): Flow<CompetitionConfig?>

    @Query("SELECT * FROM competition_config WHERE competition_id = :competitionId LIMIT 1")
    suspend fun getByCompetitionId(competitionId: Long): CompetitionConfig?
}
