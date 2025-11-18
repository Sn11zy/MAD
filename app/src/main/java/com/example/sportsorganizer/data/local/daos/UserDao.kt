package com.example.sportsorganizer.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sportsorganizer.data.local.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: LongArray): List<User>

    @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Insert
    suspend fun insertAll(vararg user: User)

    @Delete
    suspend fun delete(user: User)
}
