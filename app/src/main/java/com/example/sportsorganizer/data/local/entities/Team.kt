package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: Long = 0,
    @SerialName("competition_id") val competitionId: Long,
    @SerialName("team_name") val teamName: String
)

