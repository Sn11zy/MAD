# Step 4 Report: Testing, Build Process, and Known Bugs

This report outlines the testing strategy, the process for building a release APK, and details on a known bug related to database migrations.

## 1. Testing Strategy

The project currently contains boilerplate unit and instrumented tests. A comprehensive testing strategy should be implemented to ensure code quality and stability.

### Current State
-   `ExampleUnitTest.kt`: A basic local unit test that runs on the JVM.
-   `ExampleInstrumentedTest.kt`: A basic instrumented test that runs on an Android device/emulator.

### Proposed Strategy

#### a. Unit Tests (`/src/test`)
Unit tests are for testing individual components in isolation, such as ViewModels and Repositories.

-   **ViewModels**: Test the business logic within the ViewModels. For `AddCompetitionViewModel`, we can test that calling `createCompetition` correctly interacts with the repository and that the UI state is updated properly. Mocks can be used for the `CompetitionDao` and `GeocodingRepository`.
-   **Repositories**: Test that the repositories correctly handle data fetching from the API and database. For `WeatherRepository`, we can test success and error cases from the `WeatherApiService`.

#### b. UI / Instrumented Tests (`/src/androidTest`)
UI tests verify the app's user interface and user flows. They run on a device or emulator.

-   **Compose Screens**: Use `createComposeRule()` to test individual Composables. We can test that the `AddCompetitionScreen` correctly displays input fields and responds to user input.
-   **Navigation**: Test the navigation flow of the app. An example test would be to start on `HomeScreen`, click the "Organize" button, and assert that the app navigates to the `AddCompetitionScreen`.
-   **Database**: Test the DAOs to ensure database queries work as expected. These tests should run on a device and can use an in-memory database to ensure they are fast and hermetic.

## 2. Build Process for Release APK

To create a shareable and installable release APK, you need to sign it with a digital key.

**Step 1: Generate a Signing Key**
If you don't already have one, you need to generate a private signing key using the `keytool` command.

```bash
keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```
This command will prompt you for passwords for the keystore and the key, as well as some information about you. It will generate a file named `my-release-key.keystore`. **Keep this file private and secure.**

**Step 2: Configure Gradle for Signing**
Place the `.keystore` file in your `MAD/app` directory. Then, add your key's credentials to your `gradle.properties` file (or another secure location) to avoid hardcoding them in your build script:

```properties
# In gradle.properties
MYAPP_RELEASE_STORE_FILE=my-release-key.keystore
MYAPP_RELEASE_KEY_ALIAS=my-key-alias
MYAPP_RELEASE_STORE_PASSWORD=your_store_password
MYAPP_RELEASE_KEY_PASSWORD=your_key_password
```

Finally, configure the `app/build.gradle.kts` file to use these properties to sign your release build:

```kotlin
// ... inside android { ... } block

signingConfigs {
    create("release") {
        storeFile = file(System.getenv("MYAPP_RELEASE_STORE_FILE") ?: "my-release-key.keystore")
        storePassword = System.getenv("MYAPP_RELEASE_STORE_PASSWORD") ?: "your_store_password"
        keyAlias = System.getenv("MYAPP_RELEASE_KEY_ALIAS") ?: "my-key-alias"
        keyPassword = System.getenv("MYAPP_RELEASE_KEY_PASSWORD") ?: "your_key_password"
    }
}

buildTypes {
    release {
        // ...
        signingConfig = signingConfigs.getByName("release")
    }
}
```
*(Note: For better security, avoid storing passwords in plain text. Consider using the Android Keystore system or loading them from environment variables in your CI environment.)*

**Step 3: Build the APK**
You can build the signed APK using the Gradle wrapper script in your terminal:

```bash
./gradlew assembleRelease
```
The signed APK will be located in `MAD/app/build/outputs/apk/release/app-release.apk`.

## 3. Known Bugs & Limitations

### Database Migration Crash

**Symptom**:
The app crashes when a screen that accesses the database is opened (e.g., Organizer or Home screen). The user reports this is a "migration error" and that deleting app data and rerunning resolves the issue.

**Root Cause**:
This is a classic Room database migration issue. When the structure of a database table (an `@Entity`) is changed (e.g., adding a new column), Room requires a migration plan to update the database schema without losing user data. If the `@Database` version number is incremented but a corresponding `Migration` is not provided or is incorrect, Room throws an `IllegalStateException` because it cannot find a valid migration path from the old version to the new version.

The workaround of deleting app data works because it erases the old database file, forcing Room to create a new database from scratch using the latest schema, thus bypassing the need for migration.

**Solution / Mitigation**:

For development, the simplest way to avoid this crash is to tell Room to destructively recreate the database whenever a migration is needed. This is not suitable for production as it will wipe all user data on schema change.

To implement this, add `.fallbackToDestructiveMigration()` to your database builder in `MainActivity.kt`:

```kotlin
// In MainActivity.kt
val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java, "sports-organizer-db"
)
.addMigrations(MIGRATION_1_2)
.fallbackToDestructiveMigration() // Add this line
.build()
```

This will prevent the app from crashing during development if you make further schema changes. For a production app, you would need to write a correct `Migration` for every single schema change.
