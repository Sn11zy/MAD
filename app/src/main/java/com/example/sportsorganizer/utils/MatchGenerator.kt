package com.example.sportsorganizer.utils

import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.local.entities.Team
import com.example.sportsorganizer.data.repository.CompetitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

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

    fun assignGroups(teams: List<Team>, numberOfGroups: Int): List<Team> {
        if (numberOfGroups <= 1) return teams
        
        val shuffled = teams.shuffled()
        val assignedTeams = mutableListOf<Team>()
        
        shuffled.forEachIndexed { index, team ->
            val groupIndex = index % numberOfGroups
            val groupChar = ('A' + groupIndex).toString()
            assignedTeams.add(team.copy(groupName = "Group $groupChar"))
        }
        
        return assignedTeams
    }

    // Classic flat generation (Group Stage)
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
            "Group Stage", "Combined" -> generateGroupMatches(competitionId, validTeams, fieldCount, numberOfGroups)
            else -> generateGroupMatches(competitionId, validTeams, fieldCount, 1) // Fallback
        }
    }

    // Advanced Tree Generation (Knockout)
    suspend fun generateAndSaveKnockoutBracket(
        repo: CompetitionRepository,
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int
    ) {
        withContext(Dispatchers.IO) {
            val validTeams = teams.filter { it.id != 0L }.shuffled()
            val teamCount = validTeams.size
            if (teamCount < 2) return@withContext

            // Next power of 2 (e.g. 5 -> 8)
            val bracketSize = 2.0.pow(ceil(log2(teamCount.toDouble()))).toInt()
            val rounds = log2(bracketSize.toDouble()).toInt()
            
            var nextRoundMatchIds = mutableListOf<Long>()
            
            // Iterate from Final (Round N) down to Round 1
            // r=1 is Final, r=rounds is First Round
            // Wait, usually Round 1 is first. Let's use:
            // Round 1 (First), Round 2, ..., Round R (Final)
            // We generate from R down to 1 to have nextMatchIds ready.
            
            for (r in rounds downTo 1) {
                val matchesInRound = 2.0.pow(rounds - r).toInt() // Round R=1 match (Final) if r=rounds? No.
                // If rounds=3.
                // r=3 (Final): 2^(3-3)=1 match. Correct.
                // r=2 (Semi): 2^(3-2)=2 matches. Correct.
                // r=1 (Quarters): 2^(3-1)=4 matches. Correct.
                
                val currentRoundIds = mutableListOf<Long>()
                
                for (m in 0 until matchesInRound) {
                    val nextMatchId = if (nextRoundMatchIds.isNotEmpty()) {
                        nextRoundMatchIds[m / 2] // 2 matches feed into 1
                    } else null

                    val stageName = if (r == rounds) "Final" else if (r == rounds - 1) "Semi-Final" else "Round $r"
                    
                    // Determine Teams for Round 1
                    var t1Id: Long? = null
                    var t2Id: Long? = null
                    var status = "scheduled"
                    
                    if (r == 1) {
                        // Populate with actual teams
                        val t1Index = m * 2
                        val t2Index = m * 2 + 1
                        
                        if (t1Index < validTeams.size) t1Id = validTeams[t1Index].id
                        if (t2Index < validTeams.size) t2Id = validTeams[t2Index].id
                        
                        // Handle Bye (Auto-win logic could go here, or just leave as null vs null)
                        if (t1Id != null && t2Id == null) {
                            // Bye for T1
                            // We could auto-advance here, but for now let Referee see it.
                        }
                    }

                    val match = Match(
                        competitionId = competitionId,
                        fieldNumber = (m % fieldCount) + 1,
                        team1Id = t1Id,
                        team2Id = t2Id,
                        status = status,
                        stage = stageName,
                        nextMatchId = nextMatchId
                    )
                    
                    // Save and get ID
                    val savedMatch = repo.createMatch(match)
                    if (savedMatch != null) {
                        currentRoundIds.add(savedMatch.id)
                    }
                }
                nextRoundMatchIds = currentRoundIds
            }
        }
    }
    
    private fun matchesInThisRound(count: Int) = count // Helper for clarity

    // Helper for Group Stages
    private fun generateGroupMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int,
        numberOfGroups: Int
    ): List<Match> {
        val matches = mutableListOf<Match>()
        var matchCounter = 0
        
        if (numberOfGroups <= 1) {
            return generateRoundRobin(competitionId, teams, fieldCount, "Group Stage", matchCounter).first
        }

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
