package com.example.sportsorganizer.domain.model

/**
 * Domain model representing a competition in simplified form.
 *
 * This is a lightweight representation used within the domain layer
 * for displaying competition summaries.
 *
 * @property id The unique identifier for the competition
 * @property competitionName The name of the competition
 * @property organizer The name of the organizer who created the competition
 */
data class Competition(
    val id: Long,
    val competitionName: String?,
    val organizer: String?,
)
