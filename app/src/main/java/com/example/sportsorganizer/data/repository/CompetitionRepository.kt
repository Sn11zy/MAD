package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.local.entities.Competition
import com.example.sportsorganizer.data.local.entities.Match
import com.example.sportsorganizer.data.local.entities.Team
import com.example.sportsorganizer.data.remote.SupabaseModule
import com.example.sportsorganizer.utils.MatchGenerator
import com.example.sportsorganizer.utils.StandingsCalculator
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CompetitionRepository {

    private val client = SupabaseModule.client

    suspend fun getAllCompetitions(): List<Competition> {
        return withContext(Dispatchers.IO) {
            client.from("competitions").select().decodeList<Competition>()
        }
    }

    suspend fun getCompetitionById(id: Long): Competition? {
        return withContext(Dispatchers.IO) {
            client.from("competitions").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Competition>()
        }
    }

    suspend fun createCompetition(competition: Competition): Competition? {
        return withContext(Dispatchers.IO) {
             client.from("competitions").insert(competition) {
                 select()
             }.decodeSingleOrNull<Competition>()
        }
    }

    suspend fun updateCompetition(competition: Competition) {
        withContext(Dispatchers.IO) {
            val updateData = buildJsonObject {
                competition.gameDuration?.let { put("game_duration", it) }
                competition.winningScore?.let { put("winning_score", it) }
                competition.numberOfGroups?.let { put("number_of_groups", it) }
                competition.qualifiersPerGroup?.let { put("qualifiers_per_group", it) }
                put("points_per_win", competition.pointsPerWin)
                put("points_per_draw", competition.pointsPerDraw)
            }
            
            // Only update if there is data to update
            if (updateData.isNotEmpty()) {
                client.from("competitions").update(updateData) {
                    filter {
                        eq("id", competition.id)
                    }
                }
            }
        }
    }

    suspend fun deleteCompetition(id: Long) {
        withContext(Dispatchers.IO) {
            client.from("competitions").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    // Teams
    suspend fun createTeams(teams: List<Team>): List<Team> {
        return withContext(Dispatchers.IO) {
            client.from("teams").insert(teams) {
                select()
            }.decodeList<Team>()
        }
    }

    suspend fun updateTeams(teams: List<Team>) {
        withContext(Dispatchers.IO) {
            // Using loop + update instead of upsert to avoid "cannot insert non-DEFAULT id" issues
            teams.forEach { team ->
                val updateData = buildJsonObject {
                    put("team_name", team.teamName)
                    team.groupName?.let { put("group_name", it) }
                }
                
                client.from("teams").update(updateData) {
                    filter {
                        eq("id", team.id)
                    }
                }
            }
        }
    }

    suspend fun getTeamsForCompetition(competitionId: Long): List<Team> {
        return withContext(Dispatchers.IO) {
            client.from("teams").select {
                filter {
                    eq("competition_id", competitionId)
                }
            }.decodeList<Team>()
        }
    }

    // Matches
    suspend fun createMatches(matches: List<Match>) {
        withContext(Dispatchers.IO) {
            client.from("matches").insert(matches)
        }
    }
    
    suspend fun createMatch(match: Match): Match? {
        return withContext(Dispatchers.IO) {
            client.from("matches").insert(match) {
                select()
            }.decodeSingleOrNull<Match>()
        }
    }

    suspend fun getMatchesForCompetition(competitionId: Long): List<Match> {
        return withContext(Dispatchers.IO) {
            client.from("matches").select {
                filter {
                    eq("competition_id", competitionId)
                }
            }.decodeList<Match>()
        }
    }
    
    suspend fun getMatchesForField(competitionId: Long, fieldNumber: Int): List<Match> {
        return withContext(Dispatchers.IO) {
            client.from("matches").select {
                filter {
                    eq("competition_id", competitionId)
                    eq("field_number", fieldNumber)
                }
            }.decodeList<Match>()
        }
    }
    
    suspend fun getMatchById(id: Long): Match? {
        return withContext(Dispatchers.IO) {
            client.from("matches").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Match>()
        }
    }

    suspend fun updateMatch(match: Match) {
        withContext(Dispatchers.IO) {
            val updateData = buildJsonObject {
                put("score1", match.score1)
                put("score2", match.score2)
                put("status", match.status)
                match.stage?.let { put("stage", it) }
                match.team1Id?.let { put("team1_id", it) }
                match.team2Id?.let { put("team2_id", it) }
                match.nextMatchId?.let { put("next_match_id", it) }
            }
            client.from("matches").update(updateData) {
                filter {
                    eq("id", match.id)
                }
            }
        }
    }
    
    suspend fun generateKnockoutStage(competitionId: Long) {
        val teams = getTeamsForCompetition(competitionId)
        val matches = getMatchesForCompetition(competitionId)
        val competition = getCompetitionById(competitionId) ?: return
        
        // Check if already generated
        if (matches.any { it.stage?.contains("Round") == true || it.stage?.contains("Semi") == true || it.stage?.contains("Final") == true }) return

        val qualifiedTeams = mutableListOf<Team>()
        
        val groups = teams.groupBy { it.groupName ?: "Group A" }
        val qualifiersPerGroup = competition.qualifiersPerGroup ?: 2
        
        groups.forEach { (_, groupTeams) ->
            val groupMatches = matches.filter { it.stage?.startsWith("Group") == true }
            val stats = StandingsCalculator.calculateStandings(groupMatches, groupTeams.map { it.id })
            
            val topStats = stats.take(qualifiersPerGroup)
            val topTeamIds = topStats.map { it.teamId }
            qualifiedTeams.addAll(teams.filter { it.id in topTeamIds })
        }
        
        if (qualifiedTeams.isNotEmpty()) {
            MatchGenerator.generateAndSaveKnockoutBracket(this, competitionId, qualifiedTeams, competition.fieldCount ?: 1)
        }
    }
}
