package com.example.sportsorganizer.utils

import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.local.entities.Team

object MatchGenerator {

    fun generateTeams(competitionId: Long, numberOfTeams: Int): List<Team> {
        val teams = mutableListOf<Team>()
        for (i in 1..numberOfTeams) {
            teams.add(
                Team(
                    competitionId = competitionId,
                    teamName = "Team $i"
                )
            )
        }
        return teams
    }

    fun generateMatches(
        competitionId: Long,
        teams: List<Team>,
        tournamentMode: String,
        fieldCount: Int
    ): List<Match> {
        // Filter out teams without valid IDs just in case
        val validTeams = teams.filter { it.id != 0L }
        if (validTeams.size < 2) return emptyList()

        return when (tournamentMode) {
            "Knockout" -> generateKnockoutMatches(competitionId, validTeams, fieldCount)
            "Group Stage" -> generateRoundRobinMatches(competitionId, validTeams, fieldCount)
            "Combined" -> generateRoundRobinMatches(competitionId, validTeams, fieldCount) // Stage 1
            else -> generateRoundRobinMatches(competitionId, validTeams, fieldCount) // Default
        }
    }

    private fun generateKnockoutMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        val teamIds = teams.map { it.id }
        
        // Simple pairing for Round 1
        // 0 vs 1, 2 vs 3, etc.
        var matchCounter = 0
        for (i in 0 until teamIds.size step 2) {
            if (i + 1 < teamIds.size) {
                val field = (matchCounter % fieldCount) + 1
                matches.add(
                    Match(
                        competitionId = competitionId,
                        fieldNumber = field,
                        team1Id = teamIds[i],
                        team2Id = teamIds[i + 1],
                        status = "scheduled"
                    )
                )
                matchCounter++
            }
            // If teams.size is odd, the last team (teamIds[i]) gets a Bye (no match generated)
        }
        return matches
    }

    private fun generateRoundRobinMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        val teamIds = teams.map { it.id }
        var matchCounter = 0

        for (i in teamIds.indices) {
            for (j in i + 1 until teamIds.size) {
                val field = (matchCounter % fieldCount) + 1
                matches.add(
                    Match(
                        competitionId = competitionId,
                        fieldNumber = field,
                        team1Id = teamIds[i],
                        team2Id = teamIds[j],
                        status = "scheduled"
                    )
                )
                matchCounter++
            }
        }
        return matches
    }
}
