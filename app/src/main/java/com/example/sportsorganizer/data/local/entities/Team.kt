package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a team participating in a competition.
 *
 * This data class maps to the `teams` table in the Supabase database.
 *
 * @property id The unique identifier for the team.
 * @property competitionId The ID of the competition this team belongs to.
 * @property teamName The name of the team.
 * @property groupName The name of the group the team is assigned to (e.g., "Group A") for group stage modes.
 */
@Serializable
data class Team(
    val id: Long = 0,
    @SerialName("competition_id") val competitionId: Long,
    @SerialName("team_name") val teamName: String,
    @SerialName("group_name") val groupName: String? = null
)
