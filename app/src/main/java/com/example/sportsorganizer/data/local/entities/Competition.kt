package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "competition")
data class Competition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "competition_name") val competitionName: String,
    @ColumnInfo(name = "organizer_id") val organizer: Long,
    @ColumnInfo(name = "event_date") val eventDate: String,
    @ColumnInfo(name = "latitude") val latitude: Double = 0.0,
    @ColumnInfo(name = "longitude") val longitude: Double = 0.0,
    @ColumnInfo(name = "address") val address: String? = null
)
