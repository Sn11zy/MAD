package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Competition(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "competition_name") val competitionName: String?,
    @ColumnInfo(name = "last_name") val organizer: String?
)