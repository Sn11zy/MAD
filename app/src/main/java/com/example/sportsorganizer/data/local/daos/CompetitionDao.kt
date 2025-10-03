package com.example.sportsorganizer.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sportsorganizer.data.local.entities.Competition

@Dao
interface CompetitionDao {
    @Query("SELECT * FROM competition")
    fun getAll(): List<Competition>

    @Query("SELECT * FROM competition WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Competition>

    @Query("SELECT * FROM competition WHERE competition_name LIKE :name LIMIT 1")
    fun findByName(name: String): Competition

    @Insert
    fun insertAll(vararg competition: Competition)

    @Delete
    fun delete(competition: Competition)
}