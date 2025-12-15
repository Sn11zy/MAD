package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
    val id: Long = 0,
    @SerialName("user_id") val userId: Long,
    @SerialName("competition_name") val competitionName: String,
    val date: String? = null, // Creation timestamp
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("referee_password") val refereePassword: String,
    @SerialName("competition_password") val competitionPassword: String?,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date") val endDate: String?,
    val sport: String?,
    @SerialName("field_count") val fieldCount: Int?,
    @SerialName("scoring_type") val scoringType: String?, // 'time' or 'points'
    @SerialName("number_of_teams") val numberOfTeams: Int?,
    @SerialName("tournament_mode") val tournamentMode: String? // 'knockout', 'group', 'combined'
)
