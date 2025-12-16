package com.example.sportsorganizer.utils

import com.example.sportsorganizer.data.local.entities.Match

/**
 * Data class representing the statistical performance of a team.
 *
 * @property teamId The ID of the team.
 * @property played Total matches played.
 * @property won Total matches won.
 * @property drawn Total matches drawn.
 * @property lost Total matches lost.
 * @property points Total points accumulated (Win=3, Draw=1 typically).
 * @property goalsFor Total goals/points scored by the team.
 * @property goalsAgainst Total goals/points conceded by the team.
 */
data class TeamStats(
    val teamId: Long,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var points: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
) {
    /**
     * Calculates the goal difference (Goals For - Goals Against).
     */
    val goalDifference: Int
        get() = goalsFor - goalsAgainst
}

/**
 * Utility object for calculating tournament standings.
 */
object StandingsCalculator {
    /**
     * Calculates the standings for a set of matches and teams.
     *
     * Processes finished matches to compute wins, losses, draws, and points.
     * Sorts the results based on Points -> Goal Difference -> Goals For.
     *
     * @param matches The list of matches to process.
     * @param teamIds A list of all team IDs involved (ensures all teams appear even if 0 played).
     * @return A sorted list of [TeamStats] from first to last place.
     */
    fun calculateStandings(
        matches: List<Match>,
        teamIds: List<Long>,
    ): List<TeamStats> {
        val statsMap = teamIds.associateWith { TeamStats(it) }

        matches.forEach { match ->

            if (match.status == "finished") {
                val t1 = statsMap[match.team1Id]
                val t2 = statsMap[match.team2Id]

                if (t1 != null && t2 != null) {
                    t1.played++
                    t2.played++

                    t1.goalsFor += match.score1
                    t1.goalsAgainst += match.score2
                    t2.goalsFor += match.score2
                    t2.goalsAgainst += match.score1

                    if (match.score1 > match.score2) {
                        t1.won++
                        t1.points += 3
                        t2.lost++
                    } else if (match.score2 > match.score1) {
                        t2.won++
                        t2.points += 3
                        t1.lost++
                    } else {
                        t1.drawn++
                        t1.points += 1
                        t2.drawn++
                        t2.points += 1
                    }
                }
            }
        }

        return statsMap.values.sortedWith(
            compareByDescending<TeamStats> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor },
        )
    }
}
