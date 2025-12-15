package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "competition")
@Serializable
data class Competition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "competition_name") @SerialName("competition_name") val competitionName: String,
    @ColumnInfo(name = "organizer_id") @SerialName("organizer_id") val organizer: Long,
    @ColumnInfo(name = "event_date") @SerialName("event_date") val eventDate: String,
    @ColumnInfo(name = "latitude") val latitude: Double = 0.0,
    @ColumnInfo(name = "longitude") val longitude: Double = 0.0,
    @ColumnInfo(name = "address") val address: String? = null
)
