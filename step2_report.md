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

- **Database lifetime management**
  - Challenge: Avoid creating multiple Room instances from composables.
  - Solution: Build the DB once in `MainActivity` and pass DAOs to screens (simple, DI-ready later).

- **Gradle plugin conflict (kapt)**
  - Challenge: "plugin already on classpath with an unknown version" error for `kapt`.
  - Solution: Switched to unversioned `id("org.jetbrains.kotlin.kapt")` plugin in the app module and kept Room `kapt` dependency.

### Known limitations / next steps
- **Passwords in plain text**
  - Not for production. Next: hash (e.g., BCrypt/Argon2) or avoid storing passwords locally.

- **Primary key generation**
  - Currently uses `System.currentTimeMillis()` in ViewModels. Next: use `@PrimaryKey(autoGenerate = true)` and return inserted IDs from Room.

- **Data integrity**
  - No uniqueness constraints yet (e.g., unique `username`). Next: Room `@Entity(indices = [Index(value=["username"], unique=true)])` and error handling.

- **Migrations**
  - Version is `1`, no migrations configured. During active development, consider `fallbackToDestructiveMigration()`; later add real migrations and export schemas.

- **Testing**
  - Next: instrumented DAO tests (`androidTest`) and unit tests for ViewModels.


