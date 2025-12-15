package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
    val id: Long = 0,
    @SerialName("competition_name") val competitionName: String,
    @SerialName("user_id") val userId: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("referee_password") val refereePassword: String? = null,
    @SerialName("competition_password") val competitionPassword: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val sport: String? = null,
    @SerialName("field_count") val fieldCount: Int? = null,
    @SerialName("scoring_type") val scoringType: String?, // 'Time' or 'Points'
    @SerialName("number_of_teams") val numberOfTeams: Int?,
    @SerialName("tournament_mode") val tournamentMode: String?, // 'Knockout', 'Group Stage', 'Combined'
    @SerialName("game_duration") val gameDuration: Int? = null,
    @SerialName("winning_score") val winningScore: Int? = null,
    
    // New Fields
    @SerialName("number_of_groups") val numberOfGroups: Int? = null,
    @SerialName("qualifiers_per_group") val qualifiersPerGroup: Int? = null,
    @SerialName("points_per_win") val pointsPerWin: Int = 3,
    @SerialName("points_per_draw") val pointsPerDraw: Int = 1
)
