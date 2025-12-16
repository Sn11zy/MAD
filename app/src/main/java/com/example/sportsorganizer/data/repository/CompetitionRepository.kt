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

/**
 * Repository class for managing Competition-related data operations.
 *
 * Handles interactions with the remote Supabase database for Competitions,
 * Matches, and Teams. It also encapsulates logic for generating tournament
 * stages (Knockout) based on results.
 */
class CompetitionRepository {
    private val client = SupabaseModule.client

    /**
     * Fetches all competitions from the database.
     *
     * @return A list of [Competition] objects.
     */
    suspend fun getAllCompetitions(): List<Competition> =
        withContext(Dispatchers.IO) {
            client.from("competitions").select().decodeList<Competition>()
        }

    /**
     * Fetches a specific competition by its ID.
     *
     * @param id The ID of the competition to retrieve.
     * @return The [Competition] object if found, null otherwise.
     */
    suspend fun getCompetitionById(id: Long): Competition? =
        withContext(Dispatchers.IO) {
            client
                .from("competitions")
                .select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingleOrNull<Competition>()
        }

    /**
     * Creates a new competition in the database.
     *
     * @param competition The [Competition] object to create.
     * @return The created [Competition] with its generated ID, or null on failure.
     */
    suspend fun createCompetition(competition: Competition): Competition? =
        withContext(Dispatchers.IO) {
            client
                .from("competitions")
                .insert(competition) {
                    select()
                }.decodeSingleOrNull<Competition>()
        }

    /**
     * Updates an existing competition's configuration.
     *
     * @param competition The [Competition] object containing updated values.
     */
    suspend fun updateCompetition(competition: Competition) {
        withContext(Dispatchers.IO) {
            val updateData =
                buildJsonObject {
                    put("competition_name", competition.competitionName)
                    competition.refereePassword?.let { put("referee_password", it) }
                    competition.competitionPassword?.let { put("competition_password", it) }
                    competition.startDate?.let { put("start_date", it) }
                    competition.endDate?.let { put("end_date", it) }
                    competition.sport?.let { put("sport", it) }
                    competition.fieldCount?.let { put("field_count", it) }
                    competition.scoringType?.let { put("scoring_type", it) }
                    competition.tournamentMode?.let { put("tournament_mode", it) }

                    competition.gameDuration?.let { put("game_duration", it) }
                    competition.winningScore?.let { put("winning_score", it) }
                    competition.numberOfGroups?.let { put("number_of_groups", it) }
                    competition.qualifiersPerGroup?.let { put("qualifiers_per_group", it) }
                    put("points_per_win", competition.pointsPerWin)
                    put("points_per_draw", competition.pointsPerDraw)
                }

            if (updateData.isNotEmpty()) {
                client.from("competitions").update(updateData) {
                    filter {
                        eq("id", competition.id)
                    }
                }
            }
        }
    }

    /**
     * Deletes a competition by its ID.
     *
     * @param id The ID of the competition to delete.
     */
    suspend fun deleteCompetition(id: Long) {
        withContext(Dispatchers.IO) {
            // First delete related matches
            client.from("matches").delete {
                filter {
                    eq("competition_id", id)
                }
            }
            client.from("teams").delete {
                filter {
                    eq("competition_id", id)
                }
            }
            client.from("competitions").delete {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    /**
     * Deletes all matches associated with a competition.
     *
     * @param competitionId The ID of the competition.
     */
    suspend fun deleteMatchesForCompetition(competitionId: Long) {
        withContext(Dispatchers.IO) {
            client.from("matches").delete {
                filter {
                    eq("competition_id", competitionId)
                }
            }
        }
    }

    /**
     * Deletes all teams associated with a competition.
     *
     * @param competitionId The ID of the competition.
     */
    suspend fun deleteTeamsForCompetition(competitionId: Long) {
        withContext(Dispatchers.IO) {
            client.from("teams").delete {
                filter {
                    eq("competition_id", competitionId)
                }
            }
        }
    }

    // Teams

    /**
     * Creates multiple teams in the database.
     *
     * @param teams A list of [Team] objects to create.
     * @return The list of created [Team] objects with generated IDs.
     */
    suspend fun createTeams(teams: List<Team>): List<Team> =
        withContext(Dispatchers.IO) {
            client
                .from("teams")
                .insert(teams) {
                    select()
                }.decodeList<Team>()
        }

    /**
     * Updates multiple teams in the database.
     *
     * Used for renaming teams or assigning them to groups.
     *
     * @param teams A list of [Team] objects with updated information.
     */
    suspend fun updateTeams(teams: List<Team>) {
        withContext(Dispatchers.IO) {
            // Using loop + update instead of upsert to avoid "cannot insert non-DEFAULT id" issues
            teams.forEach { team ->
                val updateData =
                    buildJsonObject {
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

    /**
     * Fetches all teams belonging to a specific competition.
     *
     * @param competitionId The ID of the competition.
     * @return A list of [Team] objects.
     */
    suspend fun getTeamsForCompetition(competitionId: Long): List<Team> =
        withContext(Dispatchers.IO) {
            client
                .from("teams")
                .select {
                    filter {
                        eq("competition_id", competitionId)
                    }
                }.decodeList<Team>()
        }

    // Matches

    /**
     * Creates multiple matches in the database.
     *
     * @param matches A list of [Match] objects to create.
     */
    suspend fun createMatches(matches: List<Match>) {
        withContext(Dispatchers.IO) {
            client.from("matches").insert(matches)
        }
    }

    /**
     * Creates a single match in the database.
     *
     * @param match The [Match] object to create.
     * @return The created [Match] with generated ID, or null on failure.
     */
    suspend fun createMatch(match: Match): Match? =
        withContext(Dispatchers.IO) {
            client
                .from("matches")
                .insert(match) {
                    select()
                }.decodeSingleOrNull<Match>()
        }

    /**
     * Fetches all matches for a specific competition.
     *
     * @param competitionId The ID of the competition.
     * @return A list of [Match] objects.
     */
    suspend fun getMatchesForCompetition(competitionId: Long): List<Match> =
        withContext(Dispatchers.IO) {
            client
                .from("matches")
                .select {
                    filter {
                        eq("competition_id", competitionId)
                    }
                }.decodeList<Match>()
        }

    /**
     * Fetches matches assigned to a specific field in a competition.
     *
     * @param competitionId The ID of the competition.
     * @param fieldNumber The field number.
     * @return A list of [Match] objects.
     */
    suspend fun getMatchesForField(
        competitionId: Long,
        fieldNumber: Int,
    ): List<Match> =
        withContext(Dispatchers.IO) {
            client
                .from("matches")
                .select {
                    filter {
                        eq("competition_id", competitionId)
                        eq("field_number", fieldNumber)
                    }
                }.decodeList<Match>()
        }

    /**
     * Fetches a single match by its ID.
     *
     * @param id The ID of the match.
     * @return The [Match] object if found, null otherwise.
     */
    suspend fun getMatchById(id: Long): Match? =
        withContext(Dispatchers.IO) {
            client
                .from("matches")
                .select {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingleOrNull<Match>()
        }

    /**
     * Updates an existing match.
     *
     * Handles score updates, status changes, and advancing teams (via stage or nextMatchId).
     *
     * @param match The [Match] object with updated values.
     */
    suspend fun updateMatch(match: Match) {
        withContext(Dispatchers.IO) {
            val updateData =
                buildJsonObject {
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

    /**
     * Generates the knockout stage for a "Combined" tournament mode.
     *
     * Calculates standings from the Group Stage, identifies qualified teams,
     * and generates the bracket structure for the knockout phase.
     *
     * @param competitionId The ID of the competition.
     * @return The number of new matches created, or error codes (-1 for already exists, 0 for none).
     */
    suspend fun generateKnockoutStage(competitionId: Long): Int {
        println("DEBUG: generateKnockoutStage called for $competitionId")
        val teams = getTeamsForCompetition(competitionId)
        val matches = getMatchesForCompetition(competitionId)
        val competition = getCompetitionById(competitionId) ?: return 0

        println("DEBUG: Teams: ${teams.size}, Matches: ${matches.size}, Mode: ${competition.tournamentMode}")

        // Check if already generated
        if (matches.any {
                it.stage?.contains(
                    "Round",
                ) == true || it.stage?.contains("Semi") == true || it.stage?.contains("Final") == true
            }
        ) {
            println("DEBUG: Knockout matches already exist.")
            return -1
        }

        val qualifiedTeams = mutableListOf<Team>()

        val groups = teams.groupBy { it.groupName ?: "Group A" }
        println("DEBUG: Groups found: ${groups.keys}")
        val qualifiersPerGroup = competition.qualifiersPerGroup ?: 2

        groups.forEach { (name, groupTeams) ->
            val groupMatches = matches.filter { it.stage?.startsWith("Group") == true }
            println("DEBUG: Processing group '$name', teams in group: ${groupTeams.size}, total group matches: ${groupMatches.size}")

            val stats = StandingsCalculator.calculateStandings(groupMatches, groupTeams.map { it.id })

            val topStats = stats.take(qualifiersPerGroup)
            println("DEBUG: Top stats selected: ${topStats.size} (Qualifier limit: $qualifiersPerGroup)")

            val topTeamIds = topStats.map { it.teamId }
            qualifiedTeams.addAll(teams.filter { it.id in topTeamIds })
        }

        println("DEBUG: Total Qualified Teams: ${qualifiedTeams.size}")

        if (qualifiedTeams.isNotEmpty()) {
            val initialCount = getMatchesForCompetition(competitionId).size
            MatchGenerator.generateAndSaveKnockoutBracket(this, competitionId, qualifiedTeams, competition.fieldCount ?: 1)
            val finalCount = getMatchesForCompetition(competitionId).size
            println("DEBUG: New matches created: ${finalCount - initialCount}")
            return finalCount - initialCount
        }
        return 0
    }
}
