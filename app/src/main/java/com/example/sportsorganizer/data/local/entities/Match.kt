package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: Long = 0,
    @SerialName("competition_id") val competitionId: Long,
    @SerialName("field_number") val fieldNumber: Int?,
    @SerialName("team1_id") val team1Id: Long?,
    @SerialName("team2_id") val team2Id: Long?,
    val score1: Int = 0,
    val score2: Int = 0,
    val status: String = "scheduled", // 'scheduled', 'ongoing', 'paused', 'finished'
    @SerialName("start_time") val startTime: String? = null
)

