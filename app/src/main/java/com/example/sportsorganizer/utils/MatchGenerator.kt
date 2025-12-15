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

    /**
     * Assigns teams to groups. Returns the modified list of teams.
     */
    fun assignGroups(teams: List<Team>, numberOfGroups: Int): List<Team> {
        if (numberOfGroups <= 1) return teams // No groups or just 1 (default)
        
        // Shuffle teams for random assignment
        val shuffled = teams.shuffled()
        val assignedTeams = mutableListOf<Team>()
        
        shuffled.forEachIndexed { index, team ->
            val groupIndex = index % numberOfGroups
            val groupChar = ('A' + groupIndex).toString() // Group A, B, C...
            assignedTeams.add(team.copy(groupName = "Group $groupChar"))
        }
        
        return assignedTeams
    }

    fun generateMatches(
        competitionId: Long,
        teams: List<Team>,
        tournamentMode: String,
        fieldCount: Int,
        numberOfGroups: Int = 1
    ): List<Match> {
        val validTeams = teams.filter { it.id != 0L }
        if (validTeams.size < 2) return emptyList()

        return when (tournamentMode) {
            "Knockout" -> generateKnockoutMatches(competitionId, validTeams, fieldCount)
            "Group Stage", "Combined" -> generateGroupMatches(competitionId, validTeams, fieldCount, numberOfGroups)
            else -> generateGroupMatches(competitionId, validTeams, fieldCount, 1)
        }
    }

    private fun generateKnockoutMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        val teamIds = teams.map { it.id }
        
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
                        status = "scheduled",
                        stage = "Round 1"
                    )
                )
                matchCounter++
            }
        }
        return matches
    }

    private fun generateGroupMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int,
        numberOfGroups: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        var matchCounter = 0
        
        if (numberOfGroups <= 1) {
            // Single group (all vs all)
            return generateRoundRobin(competitionId, teams, fieldCount, "Group Stage", matchCounter).first
        }

        // Split teams by group
        // Assuming teams have 'groupName' set. If not, we can't really split them well here 
        // without knowing the logic used in assignGroups. 
        // But assignGroups should have been called first.
        
        val groups = teams.groupBy { it.groupName ?: "Group A" }
        
        groups.forEach { (groupName, groupTeams) ->
            val (groupMatches, newCounter) = generateRoundRobin(
                competitionId, 
                groupTeams, 
                fieldCount, 
                groupName, 
                matchCounter
            )
            matches.addAll(groupMatches)
            matchCounter = newCounter
        }

        return matches
    }

    private fun generateRoundRobin(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int,
        stageName: String,
        startCounter: Int
    ): Pair<List<Match>, Int> {
        val matches = mutableListOf<Match>()
        val teamIds = teams.map { it.id }
        var matchCounter = startCounter

        for (i in teamIds.indices) {
            for (j in i + 1 until teamIds.size) {
                val field = (matchCounter % fieldCount) + 1
                matches.add(
                    Match(
                        competitionId = competitionId,
                        fieldNumber = field,
                        team1Id = teamIds[i],
                        team2Id = teamIds[j],
                        status = "scheduled",
                        stage = stageName
                    )
                )
                matchCounter++
            }
        }
        return Pair(matches, matchCounter)
    }
}
