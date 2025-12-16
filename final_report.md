# Final Report: Sports Organizer App

## 1. Project Overview & Motivation

**Sports Organizer** is a native Android application developed to address the logistical challenges inherent in organizing local sports tournaments. Traditionally, small-scale tournaments rely on pen-and-paper schedules, manual scorekeeping, and scattered communication channels. This often results in confusion regarding match times, delayed results, and a lack of transparency for participants.

Our solution digitally transforms this process by providing a centralized platform. It serves three distinct user roles:
1.  **Organizers:** Who need powerful tools to configure tournaments, manage teams, and generate schedules automatically.
2.  **Referees:** Who require a simple, secure interface to report live scores from the field.
3.  **Spectators/Competitors:** Who demand real-time access to standings, fixtures, and results without barriers (such as mandatory login).

The application is built using modern Android development practices, leveraging **Jetpack Compose** for the UI and **Supabase** for a robust, real-time backend.

---

## 2. Detailed Feature Specification

### 2.1 Organizer Workflow
The core of the application allows organizers to create bespoke tournaments.
*   **Dynamic Configuration:** Organizers can define the sport, the number of available fields, and specific scoring rules (e.g., "Points based" vs. "Time based").
*   **Tournament Modes:**
    *   *Knockout:* Automatically generates a single-elimination bracket tree based on the number of teams, handling byes if necessary.
    *   *Group Stage:* Uses a Round-Robin algorithm to ensure every team plays every other team in their group.
    *   *Combined:* A hybrid mode where a group stage is followed by a knockout phase for the top qualifiers.
*   **Team Management:** The app simplifies setup by auto-generating placeholder teams (e.g., "Team 1", "Team 2"). Organizers can then rename these or assign them to specific groups.

### 2.2 Referee & Match Management
*   **Secure Access:** Referees do not need full accounts. Instead, they use a specific "Referee Password" set by the organizer for each competition. This lowers the barrier to entry for volunteers.
*   **Live Updates:** Referees can update match status to "In Progress" or "Finished" and input scores. These updates are immediately reflected in the database.
*   **Field Filtering:** To avoid clutter, referees can filter the match list to show only games scheduled on their specific field.

### 2.3 Spectator Experience
*   **Frictionless Access:** Competitors can view all public competitions without creating an account.
*   **Automated Standings:** The app includes a logic engine that calculates league tables in real-time. It processes:
    *   Points (customizable, e.g., 3 for win, 1 for draw).
    *   Goal Difference (Goals For - Goals Against).
    *   Wins, Losses, and Draws.
*   **Weather Integration:** Understanding that many local sports are played outdoors, the app provides a weather forecast for the specific event location and date.

---

## 3. Technical Architecture

The application is architected using the **MVVM (Model-View-ViewModel)** pattern, ensuring a scalable, testable, and maintainable codebase.

### 3.1 UI Layer (View)
We utilized **Jetpack Compose** exclusively for the UI. This declarative approach allowed for rapid UI development and dynamic state handling.
*   **Navigation:** `Navigation Compose` manages the flow between screens (Home -> Organize -> Team Naming -> Detail).
*   **State Hoisting:** Stateless composables receive data via parameters, making components reusable and easier to preview.

### 3.2 ViewModel Layer
ViewModels act as the bridge between the Repository and the UI. They are responsible for:
*   Exposing data streams via `StateFlow` and `SharedFlow`.
*   Handling user actions (e.g., "Create Competition" clicks).
*   Managing `viewModelScope` to launch coroutines for asynchronous operations.

### 3.3 Data Layer (Model & Repository)
*   **Repository Pattern:** We implemented `CompetitionRepository` and `UserRepository` to abstract data sources. The UI is unaware of whether data comes from a local cache or the network.
*   **Backend (Supabase):** We chose Supabase as an open-source Firebase alternative. It provides a PostgreSQL database with real-time capabilities.
    *   *Database Schema:*
        *   `users`: Stores authentication details and profiles.
        *   `competitions`: Stores configuration, passwords, and dates.
        *   `teams`: Relational table linking teams to competitions.
        *   `matches`: Stores scores, status, field numbers, and stage info.
*   **Remote APIs:** A custom `WeatherRepository` interfaces with an external weather API using **Retrofit**, providing localized forecasts.

---

## 4. Key Implementation Details & Algorithms

### 4.1 Match Generation Algorithms
One of the most complex components is the `MatchGenerator` utility.
*   **Round Robin Logic:** For group stages, the algorithm iterates through team indices to create unique pairs. It also attempts to distribute matches across available fields to optimize the schedule.
*   **Knockout Bracket Generation:** The app calculates the nearest power of 2 to determine the bracket size (e.g., 5 teams -> 8-team bracket). It generates placeholders for "Finals", "Semi-Finals", etc., and links matches so that winners automatically advance in the database structure.

### 4.2 Standings Calculator
The `StandingsCalculator` is a pure Kotlin object that takes a list of `Match` entities and `Team` entities. It iterates through finished matches to accumulate stats.
*   *Sorting Logic:* It implements a custom `Comparator` to sort teams first by Points, then by Goal Difference, and finally by Goals Scored. This ensures accurate league tables compliant with standard sports rules.

### 4.3 Asynchronous Data Handling
We extensively used **Kotlin Coroutines** for all I/O operations.
*   **Parallel Fetching:** In the `CompetitionDetailScreen`, we launch parallel coroutines to fetch `Matches`, `Teams`, and `Weather` simultaneously, minimizing loading times for the user.
*   **State Management:** `MutableStateFlow` is used to emit loading states (`UiState.Loading`), success data (`UiState.Success`), or errors (`UiState.Error`), ensuring the UI always reflects the current data status.

---

## 5. Challenges & Solutions

### 5.1 Relational Data in NoSQL-like Queries
**Challenge:** While Supabase is SQL-based, the client library interactions felt similar to NoSQL document fetching. Getting a list of matches that included the *names* of the teams (which are stored in a separate table) was initially difficult without writing raw SQL.
**Solution:** We implemented client-side mapping. We fetch `Matches` and `Teams` separately in parallel and then map the `teamId` from the match to the `teamName` from the team list within the UI logic. This proved performant for the scale of local tournaments.

### 5.2 Dynamic Form Validation
**Challenge:** The "Create Competition" screen became very complex, with fields disappearing or appearing based on "Tournament Mode" or "Scoring Type".
**Solution:** We used Compose's state-driven recomposition to hide/show fields dynamically. We also implemented a comprehensive validation step in the ViewModel to ensure non-nullable fields (like `userId` for organizers) were present before attempting network calls.

### 5.3 Sandbox & Permissions
**Challenge:** During development, we faced issues with network permissions and file access when running local linting tools via the terminal.
**Solution:** We relied on the Android standard `lint` task via Gradle wrapper and ensured strict adherence to the `AndroidManifest.xml` internet permissions to allow Retrofit and Supabase traffic.

---

## 6. Future Improvements

*   **Offline First Architecture:** Currently, the app requires an internet connection. Future versions would implement **Room** (local SQLite) to cache competitions and matches, allowing referees to score games offline and sync when connection is restored.
*   **Push Notifications:** Integration with **Firebase Cloud Messaging (FCM)** to notify users when a match starts or a result is posted.
*   **Social Sharing:** Generating shareable images of the bracket or standings table so organizers can post them to social media directly from the app.
*   **Advanced Seeding:** Allowing organizers to manually seed the knockout bracket rather than relying on random assignment or group results.

---

## 7. Conclusion

Sports Organizer successfully solves the fragmentation problem of local sports management. By leveraging the power of Android and the cloud, it delivers a robust tool that saves time for organizers and improves the experience for participants. The modular architecture and clean code practices adopted ensures the project is maintainable and ready for future scalability.
