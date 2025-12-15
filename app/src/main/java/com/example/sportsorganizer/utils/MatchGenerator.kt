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

    // This returns matches. It requires the actual Team objects (with IDs) 
    // so we can link them. This means we must insert teams FIRST, get their IDs, 
    // and then call this.
    fun generateMatches(
        competitionId: Long,
        teams: List<Team>,
        tournamentMode: String,
        fieldCount: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        
        // Simple Round Robin implementation for now
        // For 'knockout', we'd need a bracket structure which is more complex
        
        // Basic Logic: Pair every team with every other team (single round robin)
        // Adjust for field count is just for scheduling, which we can skip for now or assign simple integers
        
        val teamIds = teams.map { it.id }
        if (teamIds.size < 2) return emptyList()

        var matchCounter = 0

        for (i in teamIds.indices) {
            for (j in i + 1 until teamIds.size) {
                // Determine field number (simple round robin assignment)
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

