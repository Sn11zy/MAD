package com.example.sportsorganizer.data.local.dbs

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sportsorganizer.data.local.daos.CompetitionDao
import com.example.sportsorganizer.data.local.daos.CompetitionConfigDao
import com.example.sportsorganizer.data.local.daos.UserDao
import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.User
import com.example.sportsorganizer.data.local.entities.CompetitionConfig

@Database(entities = [User::class, Competition::class, CompetitionConfig::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competitionDao(): CompetitionDao

    abstract fun competitionConfigDao(): CompetitionConfigDao

    abstract fun userDao(): UserDao
}

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE competition ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE competition ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE competition ADD COLUMN event_date TEXT NOT NULL DEFAULT ''")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the new competition_config table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS competition_config (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    competition_id INTEGER NOT NULL,
                    location_name TEXT,
                    address TEXT,
                    start_time TEXT,
                    end_time TEXT,
                    max_participants INTEGER,
                    is_confirmed INTEGER NOT NULL DEFAULT 0,
                    notes TEXT
                );
            """.trimIndent())

            // Backfill config rows from existing competition columns if they exist
            // (If your previous schema stored these columns directly on competition.)
            try {
                db.execSQL("""
                    INSERT INTO competition_config (competition_id, location_name, address, start_time, end_time, max_participants, is_confirmed, notes)
                    SELECT id, location_name, address, start_time, end_time, max_participants, is_confirmed, notes FROM competition;
                """.trimIndent())
            } catch (e: Exception) {
                // If columns do not exist, the SELECT will fail; it's safe to ignore here.
            }
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add new columns to competition_config
            db.execSQL("ALTER TABLE competition_config ADD COLUMN sport_type TEXT")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN number_of_fields INTEGER")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN scoring_mode TEXT")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN scoring_value INTEGER")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN is_sudden_death INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN number_of_sets INTEGER")
            db.execSQL("ALTER TABLE competition_config ADD COLUMN number_of_teams INTEGER")

            // Add address to competition table if not present (safe attempt)
            try {
                db.execSQL("ALTER TABLE competition ADD COLUMN address TEXT")
            } catch (e: Exception) {
                // ignore if column exists or operation unsupported
            }
        }
    }

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE competition_config DROP COLUMN location_name")
            db.execSQL("ALTER TABLE competition_config DROP COLUMN start_time")
            db.execSQL("ALTER TABLE competition_config DROP COLUMN end_time")
            db.execSQL("ALTER TABLE competition_config DROP COLUMN is_confirmed")
            db.execSQL("ALTER TABLE competition_config DROP COLUMN notes")
        }
    }
