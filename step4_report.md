# Step 4 Report: Testing, Building, and Known Issues

This report outlines the testing strategy, APK build process, and known limitations of the Sport Event Organizer application.

## 1. Testing Strategy

The testing strategy is divided into two main categories: Unit Tests and Instrumentation (UI) Tests.

### Unit Tests

Unit tests are used for validating business logic that does not require an Android device to run. They are located in the `app/src/test` directory.

-   **Target**: The `PasswordHashing.kt` utility is a prime candidate for unit testing.
-   **Method**: Tests should be written to verify that the `hashPassword` and `verifyPassword` functions work as expected. This includes:
    -   Verifying that a correct password matches its hash.
    -   Ensuring an incorrect password does not match.

### Instrumentation Tests

Instrumentation tests are used for testing UI interactions and components that rely on the Android framework, such as the database. They are located in the `app/src/androidTest` directory.

-   **Target**: The app's UI flows, such as creating a user, adding a competition, and navigating between screens.
-   **Method**: Using the Jetpack Compose test APIs, we can write tests to:
    -   Simulate user input in forms (e.g., `AddCompetitionScreen`, `UserScreen`).
    -   Verify navigation between different screens.
    -   Confirm that data from the Room database is correctly displayed in the UI.

## 2. Build Process for Release APK

Building a signed APK for release on the Google Play Store is done using standard Gradle tasks.

1.  **Generate a Signing Key**: First, a private signing key must be generated using the `keytool` command. This key is used to sign the application and must be kept private.

2.  **Configure Gradle**: The app-level `build.gradle.kts` file must be configured to use the signing key. This typically involves storing the key's credentials securely in the `gradle.properties` file and creating a `signingConfigs` block.

3.  **Build the APK**: With the signing configuration in place, the release APK can be built by running the following command in the project's root directory:
    ```bash
    ./gradlew assembleRelease
    ```
    The signed APK will be located in `MAD/app/build/outputs/apk/release/`.

## 3. Known Bugs and Limitations

-   **Database Migration Crash**: There is a known issue related to Room database migrations. When the app is updated with a new database schema, it may crash on the first launch for existing users. The current workaround is to manually clear the app's data or uninstall and reinstall the app. This forces Room to create a new database from scratch instead of performing a migration, which resolves the crash. This indicates that a required migration path is either missing or implemented incorrectly.
