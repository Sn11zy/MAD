package com.example.sportsorganizer.data.local.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single match within a competition.
 *
 * This data class maps to the `matches` table in the Supabase database.
 * It tracks the teams involved, the score, the status, and the progression
 * within the tournament structure.
 *
 * @property id The unique identifier for the match.
 * @property competitionId The ID of the competition this match belongs to.
 * @property fieldNumber The number of the field where the match is played.
 * @property team1Id The ID of the first team (nullable for placeholders).
 * @property team2Id The ID of the second team (nullable for placeholders).
 * @property score1 The score of the first team.
 * @property score2 The score of the second team.
 * @property status The current status of the match ("scheduled", "in_progress", "finished").
 * @property startTime The scheduled start time of the match.
 * @property stage The tournament stage (e.g., "Group A", "Round 1", "Quarter-Final").
 * @property nextMatchId The ID of the match where the winner of this match advances (for Knockout).
 */
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
