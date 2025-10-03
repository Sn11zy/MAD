package com.example.sportsorganizer.data.local.dbs

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.User

@Database(entities = [User::class, Competition::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun userDao(): UserDao
}