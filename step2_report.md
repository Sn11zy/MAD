## Step 2 — Short Report

### What data is stored locally
- **User (table: `user`)**
  - `id: Long` — primary key
  - `first_name: String?`
  - `last_name: String?`
  - `username: String`
  - `password: String` (plain text for now; see challenges)

- **Competition (table: `competition`)**
  - `id: Long` — primary key
  - `competition_name: String?`
  - `organizer_id: Long` — references `User.id`

- **Relation**
  - `UserWithCompetitions` — Room `@Relation` mapping between `User.id` and `Competition.organizer_id`.

- **Database**
  - `AppDatabase` (RoomDatabase v1) exposes `UserDao` and `CompetitionDao`.
  - Created once in `MainActivity` via `Room.databaseBuilder(...)` and DAOs are passed to screens/VMs.

### Challenges and solutions
- **Compose navigation and screen wiring**
  - Challenge: Introduce navigation and route structure from a blank start.
  - Solution: `NavHost` in `MainActivity` with routes for Home, Organize, Referee, Competitor, User, About.

- **ViewModel integration and threading**
  - Challenge: Insert operations should not block the UI; DAO methods are non-suspending.
  - Solution: `AddCompetitionViewModel` and `CreateUserViewModel` perform inserts on `Dispatchers.IO` and expose a `StateFlow` with `Success/Error`.

- **No feedback after pressing Create**
  - Challenge: Users saw no visible result after an insert.
  - Solution: Screens now collect the `creationResult` and show a Toast for success/error.

