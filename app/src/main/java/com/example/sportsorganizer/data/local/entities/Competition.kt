package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a competition in the sports organizer app.
 *
 * This data class maps to the `competitions` table in the Supabase database.
 * It stores configuration details for a tournament, including its format,
 * rules, and administrative credentials.
 *
 * @property id The unique identifier for the competition.
 * @property competitionName The name of the competition.
 * @property userId The ID of the organizer who created the competition.
 * @property latitude The geographical latitude for the competition location (weather).
 * @property longitude The geographical longitude for the competition location (weather).
 * @property eventDate The date of the event in "YYYY-MM-DD" format.
 * @property refereePassword The password required for referees to log in and manage matches.
 * @property competitionPassword A password for participants (optional/future use).
 * @property startDate The start date of the tournament.
 * @property endDate The end date of the tournament.
 * @property sport The type of sport (e.g., "Football", "Basketball").
 * @property fieldCount The number of fields available for matches.
 * @property scoringType The type of scoring used ("Points" or "Time").
 * @property numberOfTeams The total number of teams participating.
 * @property tournamentMode The format of the tournament ("Knockout", "Group Stage", "Combined").
 * @property gameDuration The duration of a game in minutes (if scoringType is "Time").
 * @property winningScore The score required to win a set/game (if scoringType is "Points").
 * @property numberOfGroups The number of groups for "Group Stage" or "Combined" modes.
 * @property qualifiersPerGroup The number of teams that advance from each group.
 * @property pointsPerWin The points awarded for a win (default 3).
 * @property pointsPerDraw The points awarded for a draw (default 1).
 */
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
