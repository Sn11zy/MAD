package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0, // Supabase will generate this
    @SerialName("first_name") val firstName: String?,
    @SerialName("last_name") val lastName: String?,
    val username: String,
    @SerialName("password_hash") val passwordHash: String
)
