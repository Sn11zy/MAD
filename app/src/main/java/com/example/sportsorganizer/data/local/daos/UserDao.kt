package com.example.sportsorganizer.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<User>

    @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
    fun findByUsername(username: String): User

    @Insert
    fun insertAll(vararg user: User)

    @Delete
    fun delete(user: User)
}
