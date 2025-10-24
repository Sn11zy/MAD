package com.example.sportsorganizer.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sportsorganizer.data.local.entities.Competition
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {
    @Query("SELECT * FROM competition")
    fun getAll(): Flow<List<Competition>>

    @Query("SELECT * FROM competition WHERE id = :id")
    suspend fun findById(id: Long): Competition?

    @Query("SELECT * FROM competition WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Competition>

    @Query("SELECT * FROM competition WHERE competition_name LIKE :name LIMIT 1")
    suspend fun findByName(name: String): Competition?

    @Insert
    suspend fun insertAll(vararg competition: Competition): List<Long>

    @Delete
    suspend fun delete(competition: Competition)
}