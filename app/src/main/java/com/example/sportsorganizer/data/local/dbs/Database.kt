package com.example.sportsorganizer.data.local.dbs

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.User

@Database(entities = [User::class, Competition::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao
    abstract fun userDao(): UserDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE competition ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE competition ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE competition ADD COLUMN event_date TEXT NOT NULL DEFAULT ''")
    }
}