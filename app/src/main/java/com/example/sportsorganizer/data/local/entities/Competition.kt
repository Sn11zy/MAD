package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Competition(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "competition_name") val competitionName: String?,
    @ColumnInfo(name = "organizer_id") val organizer: Long
)