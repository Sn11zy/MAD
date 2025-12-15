package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: Long = 0,
    @SerialName("competition_id") val competitionId: Long,
    @SerialName("field_number") val fieldNumber: Int?,
    @SerialName("team1_id") val team1Id: Long? = null,
    @SerialName("team2_id") val team2Id: Long? = null,
    val score1: Int = 0,
    val score2: Int = 0,
    val status: String = "scheduled", // 'scheduled', 'in_progress', 'finished'
    @SerialName("start_time") val startTime: String? = null,
    val stage: String? = null, // e.g. "Group A", "Round 1"
    @SerialName("next_match_id") val nextMatchId: Long? = null
)
