# System Architecture & Algorithms

This document provides a technical deep-dive into the internal workings of the Sports Organizer app. It covers the database schema, key algorithms for tournament generation, and the data flow architecture.

## 1. System Architecture

The app follows the **MVVM (Model-View-ViewModel)** architectural pattern to ensure separation of concerns and testability.

```mermaid
graph TD
    UI[Jetpack Compose UI] <--> VM[ViewModel]
    VM <--> Repo[Repository Layer]
    Repo <--> Remote[Supabase (PostgreSQL)]
    Repo <--> Weather[Weather API]
```

*   **UI Layer:** Built with Jetpack Compose. Reactive UI that observes state from ViewModels.
*   **ViewModel:** Holds `StateFlow` data streams. Survives configuration changes and handles business logic.
*   **Repository:** The single source of truth. It abstracts the data sources (Network/Database) from the rest of the app.

---

## 2. Database Schema (Supabase)

We utilize **Supabase** (PostgreSQL) for real-time data storage. The relational schema is designed to support various tournament modes.

### Core Tables

#### `users`
Represents the organizers.
*   `id` (BigInt): Primary Key.
*   `username` (String): Unique login identifier.
*   `password_hash` (String): Securely hashed password.

#### `competitions`
Stores configuration for a tournament.
*   `id`: Primary Key.
*   `user_id`: Foreign Key linking to `users`.
*   `tournament_mode`: Enum-like string ('Knockout', 'Group Stage', 'Combined').
*   `scoring_type`: 'Points' or 'Time'.
*   `field_count`: Number of available fields for scheduling.

#### `teams`
*   `id`: Primary Key.
*   `competition_id`: Foreign Key.
*   `team_name`: String.
*   `group_name`: (Optional) For group stages (e.g., "Group A").

#### `matches`
The central entity connecting teams and results.
*   `id`: Primary Key.
*   `competition_id`: Foreign Key.
*   `team1_id` / `team2_id`: Foreign Keys (Nullable for placeholder matches in brackets).
*   `score1` / `score2`: Integer scores.
*   `status`: 'scheduled', 'in_progress', 'finished'.
*   `next_match_id`: (Self-referencing Foreign Key) Used in Knockout trees to point to the next round.

---

## 3. Key Algorithms

### 3.1 Knockout Bracket Generation
The app dynamically generates a single-elimination bracket tree. It handles any number of teams by calculating the nearest power of 2.

**Key Logic:**
1.  Calculate `bracketSize` (e.g., 5 teams -> 8 slots).
2.  Iterate backwards from the **Final** (Round N) down to **Round 1**.
3.  Link matches: The winner of a Round 1 match automatically advances to a specific match in Round 2 via the `next_match_id`.

**Snippet (`MatchGenerator.kt`):**
```kotlin
// Calculate depth of the tree
val bracketSize = 2.0.pow(ceil(log2(teamCount.toDouble()))).toInt()
val rounds = log2(bracketSize.toDouble()).toInt()

// Generate matches from Final down to First Round
for (r in rounds downTo 1) {
    val matchesInRound = 2.0.pow(rounds - r).toInt()
    
    for (m in 0 until matchesInRound) {
        // Link to the next round match (m / 2)
        val nextMatchId = if (nextRoundMatchIds.isNotEmpty()) {
            nextRoundMatchIds[m / 2] 
        } else null
        
        // Create match with 'nextMatchId' reference
        // ...
    }
}
```

### 3.2 Round-Robin Scheduling
For "Group Stage" mode, every team must play every other team in their group.

**Algorithm:**
*   Input: List of Teams in a Group.
*   Process: Nested iteration to create unique pairs `(Team A vs Team B)`.
*   Field Assignment: Distributes matches across `fieldCount` fields using modulo arithmetic (`matchCounter % fieldCount`).

### 3.3 Standings Calculation
Real-time league tables are computed client-side by aggregating results from finished matches.

**Sorting Criteria:**
1.  **Points** (3 for Win, 1 for Draw).
2.  **Goal Difference** (Goals Scored - Goals Conceded).
3.  **Goals For** (Total Goals Scored).

**Snippet (`StandingsCalculator.kt`):**
```kotlin
return statsMap.values.sortedWith(
    compareByDescending<TeamStats> { it.points }
        .thenByDescending { it.goalDifference }
        .thenByDescending { it.goalsFor }
)
```
