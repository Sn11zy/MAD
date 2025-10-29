## Step 3 — API Integration Report

### APIs Used

*   **Weather API**: [Open-Meteo Weather Forecast API](https://open-meteo.com/)
    *   **Why**: It's free, requires no API key, provides detailed forecast data, and accepts latitude/longitude, which was perfect for our needs.
*   **Geocoding API**: [Open-Meteo Geocoding API](https://open-meteo.com/en/docs/geocoding-api)
    *   **Why**: To provide a user-friendly city search feature instead of manual latitude/longitude input. It's also free, keyless, and from the same provider as our weather API.

### Example API Endpoints

*   **Weather Forecast**:
    ```
    https://api.open-meteo.com/v1/forecast?latitude=55.67&longitude=12.56&daily=temperature_2m_max,precipitation_probability_max,wind_speed_10m_max&timezone=auto&start_date=2024-10-29&end_date=2024-10-29
    ```
*   **City Search (Geocoding)**:
    ```
    https://geocoding-api.open-meteo.com/v1/search?name=Tallinn&count=10
    ```

### Implementation Details & Key Components

*   **Networking**: Used Retrofit for API communication and Moshi for parsing JSON responses. A `RetrofitInstance` object was created to manage two separate base URLs for the weather and geocoding APIs.
*   **Repositories**:
    *   `WeatherRepository`: Fetches weather data and wraps the result in a sealed `Result` class to handle success and error states (e.g., `IOException`, `HttpException`).
    *   `GeocodingRepository`: Fetches city search results and also uses the `Result` wrapper for robust error handling.
*   **ViewModels**:
    *   `WeatherViewModel`: Manages the state for the `CompetitionDetailScreen` (`Loading`, `Success`, `Error`).
    *   `AddCompetitionViewModel`: Updated to handle the autocomplete city search logic, including debouncing user input to avoid excessive API calls.
*   **UI**:
    *   `AddCompetitionScreen`: The manual latitude and longitude fields were replaced with an autocomplete search box for a much better user experience.
    *   `CompetitionDetailScreen`: A new screen was created to display the weather forecast for a selected competition.

### Challenges and Solutions

*   **User-Friendly Location Input**: The initial plan was for users to manually enter latitude and longitude. This was improved by implementing an autocomplete city search feature using the geocoding API.
*   **Resilient JSON Parsing**: The app crashed with a `JsonDataException` when the geocoding API returned a city without a `country`. This was solved by making the `country` property nullable in our `City` data class (`String?`) and updating the UI to handle the missing value gracefully.
*   **Efficient Data Fetching**: The weather data was being re-fetched on every recomposition, causing the UI to blink. This was fixed by using a `LaunchedEffect` to fetch the data only once when the screen first loads and adding a "Refresh" button for manual updates.
*   **Android Permissions**: The app crashed with a `SecurityException` because it was trying to access the internet without permission. This was resolved by adding `<uses-permission android:name="android.permission.INTERNET" />` to the `AndroidManifest.xml`.
*   **Database on Main Thread**: The app crashed with an `IllegalStateException` when fetching data from the database on the main thread. This was fixed by making the Room DAO methods `suspend` functions, which ensures they run on a background thread.

### Implemented Data Model Changes

The `Competition` entity was updated as planned to include location and date information. The Room database was successfully migrated from version 1 to 2 to accommodate the new schema.

**`Competition.kt` (Final Entity)**

```kotlin
@Entity
data class Competition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "competition_name") val competitionName: String?,
    @ColumnInfo(name = "organizer_id") val organizer: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "event_date") val eventDate: String
)
```
