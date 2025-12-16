package com.example.sportsorganizer.utils

import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.local.entities.Team
import com.example.sportsorganizer.data.repository.CompetitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

/**
 * Utility object for generating match schedules and brackets.
 *
 * Contains logic for Round Robin generation (Group Stages) and
 * Single Elimination Brackets (Knockout Stages).
 */
object MatchGenerator {
    /**
     * Generates a list of placeholder teams for a competition.
     *
     * @param competitionId The ID of the competition.
     * @param numberOfTeams The number of teams to generate.
     * @return A list of [Team] objects named "Team 1", "Team 2", etc.
     */
    fun generateTeams(
        competitionId: Long,
        numberOfTeams: Int,
    ): List<Team> {
        val teams = mutableListOf<Team>()
        for (i in 1..numberOfTeams) {
            teams.add(
                Team(
                    competitionId = competitionId,
                    teamName = "Team $i",
                ),
            )
        }
        return teams
    }

    /**
     * Assigns teams to groups randomly.
     *
     * @param teams The list of teams to assign.
     * @param numberOfGroups The number of groups to distribute teams into.
     * @return A new list of [Team] objects with assigned `groupName`.
     */
    fun assignGroups(
        teams: List<Team>,
        numberOfGroups: Int,
    ): List<Team> {
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

    /**
     * Generates matches for a competition based on its mode.
     *
     * Handles "Group Stage" and "Combined" modes by generating Round Robin matches.
     * "Knockout" mode logic is handled separately in [generateAndSaveKnockoutBracket].
     *
     * @param competitionId The ID of the competition.
     * @param teams The participating teams.
     * @param tournamentMode The mode of the tournament.
     * @param fieldCount The number of available fields.
     * @param numberOfGroups The number of groups (for group-based modes).
     * @return A list of [Match] objects ready to be saved.
     */
    fun generateMatches(
        competitionId: Long,
        teams: List<Team>,
        tournamentMode: String,
        fieldCount: Int,
        numberOfGroups: Int = 1,
    ): List<Match> {
        val validTeams = teams.filter { it.id != 0L }
        if (validTeams.size < 2) return emptyList()

        return when (tournamentMode) {
            "Group Stage", "Combined" -> generateGroupMatches(competitionId, validTeams, fieldCount, numberOfGroups)
            else -> generateGroupMatches(competitionId, validTeams, fieldCount, 1) // Fallback
        }
    }

    /**
     * Generates and saves a knockout bracket structure to the repository.
     *
     * Creates a tree structure of matches (Final -> Semis -> Quarters etc.)
     * and populates the first round with the provided teams.
     *
     * @param repo The [CompetitionRepository] used to save matches.
     * @param competitionId The ID of the competition.
     * @param teams The teams qualifying for the knockout stage.
     * @param fieldCount Number of fields to distribute matches across.
     */
    suspend fun generateAndSaveKnockoutBracket(
        repo: CompetitionRepository,
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int,
    ) {
        withContext(Dispatchers.IO) {
            val validTeams = teams.filter { it.id != 0L }.shuffled()
            val teamCount = validTeams.size
            if (teamCount < 2) return@withContext

            val bracketSize = 2.0.pow(ceil(log2(teamCount.toDouble()))).toInt()
            val rounds = log2(bracketSize.toDouble()).toInt()

            var nextRoundMatchIds = mutableListOf<Long>()

            for (r in rounds downTo 1) {
                val matchesInRound = 2.0.pow(rounds - r).toInt()

                val currentRoundIds = mutableListOf<Long>()

                for (m in 0 until matchesInRound) {
                    val nextMatchId =
                        if (nextRoundMatchIds.isNotEmpty()) {
                            nextRoundMatchIds[m / 2]
                        } else {
                            null
                        }

                    val stageName =
                        if (r == rounds) {
                            "Final"
                        } else if (r == rounds - 1) {
                            "Semi-Final"
                        } else {
                            "Round $r"
                        }

                    var t1Id: Long? = null
                    var t2Id: Long? = null
                    var status = "scheduled"

                    if (r == 1) {
                        val t1Index = m * 2
                        val t2Index = m * 2 + 1

                        if (t1Index < validTeams.size) t1Id = validTeams[t1Index].id
                        if (t2Index < validTeams.size) t2Id = validTeams[t2Index].id
                    }

                    val match =
                        Match(
                            competitionId = competitionId,
                            fieldNumber = (m % fieldCount) + 1,
                            team1Id = t1Id,
                            team2Id = t2Id,
                            status = status,
                            stage = stageName,
                            nextMatchId = nextMatchId,
                        )

                    val savedMatch = repo.createMatch(match)
                    if (savedMatch != null) {
                        currentRoundIds.add(savedMatch.id)
                    }
                }
                nextRoundMatchIds = currentRoundIds
            }
        }
    }

    private fun generateGroupMatches(
        competitionId: Long,
        teams: List<Team>,
        fieldCount: Int,
        numberOfGroups: Int,
    ): List<Match> {
        val matches = mutableListOf<Match>()
        var matchCounter = 0

        if (numberOfGroups <= 1) {
            return generateRoundRobin(competitionId, teams, fieldCount, "Group Stage", matchCounter).first
        }

        val groups = teams.groupBy { it.groupName ?: "Group A" }

        groups.forEach { (groupName, groupTeams) ->
            val (groupMatches, newCounter) =
                generateRoundRobin(
                    competitionId,
                    groupTeams,
                    fieldCount,
                    groupName,
                    matchCounter,
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
        startCounter: Int,
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
                        stage = stageName,
                    ),
                )
                matchCounter++
            }
        }
        return Pair(matches, matchCounter)
    }
}
