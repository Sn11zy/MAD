package com.example.sportsorganizer.utils

import com.example.sportsorganizer.data.local.entities.Match

data class TeamStats(
    val teamId: Long,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var points: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0
) {
    val goalDifference: Int
        get() = goalsFor - goalsAgainst
}

object StandingsCalculator {

    fun calculateStandings(matches: List<Match>, teamIds: List<Long>): List<TeamStats> {
        val statsMap = teamIds.associateWith { TeamStats(it) }

        matches.forEach { match ->
            // Only consider matches that have a result or are in progress? 
            // Usually Standings only reflect FINISHED matches.
            // But if we want live updates, we can include 'ongoing'.
            // Let's stick to 'finished' for now to be safe, or check if scores are non-zero.
            // Actually, let's include 'finished' only.
            
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
                .thenByDescending { it.goalsFor }
        )
    }
}

