package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competition_config",
    foreignKeys = [
        ForeignKey(
            entity = Competition::class,
            parentColumns = ["id"],
            childColumns = ["competition_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["competition_id"], unique = true)]
)
data class CompetitionConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "competition_id") val competitionId: Long,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "address") val address: String? = null,
    @ColumnInfo(name = "start_time") val startTime: String? = null,
    @ColumnInfo(name = "end_time") val endTime: String? = null,
    @ColumnInfo(name = "max_participants") val maxParticipants: Int? = null,
    @ColumnInfo(name = "is_confirmed") val isConfirmed: Boolean = false,
    @ColumnInfo(name = "notes") val notes: String? = null,

    // New fields for sport configuration
    @ColumnInfo(name = "sport_type") val sportType: String? = null,
    @ColumnInfo(name = "number_of_fields") val numberOfFields: Int? = null,
    @ColumnInfo(name = "scoring_mode") val scoringMode: String? = null, // "TIME" or "POINTS"
    @ColumnInfo(name = "scoring_value") val scoringValue: Int? = null,
    @ColumnInfo(name = "is_sudden_death") val isSuddenDeath: Boolean = false,
    @ColumnInfo(name = "number_of_sets") val numberOfSets: Int? = null,
    @ColumnInfo(name = "number_of_teams") val numberOfTeams: Int? = null,
)
