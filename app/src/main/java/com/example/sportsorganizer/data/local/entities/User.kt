package com.example.sportsorganizer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class User(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "first_name") @SerialName("first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") @SerialName("last_name") val lastName: String?,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
)
