package com.example.sportsorganizer.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for Open-Meteo API endpoints.
 *
 * Defines endpoints for weather forecasts and city geocoding searches.
 */
interface WeatherApiService {
    /**
     * Fetches weather forecast data for specific coordinates and date range.
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param daily Comma-separated list of daily weather variables to fetch
     * @param timezone Timezone for the forecast (default: "auto")
     * @param startDate Start date in "YYYY-MM-DD" format
     * @param endDate End date in "YYYY-MM-DD" format
     * @return A [WeatherResponse] containing daily weather data
     */
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,precipitation_probability_max,wind_speed_10m_max",
        @Query("timezone") timezone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): WeatherResponse

    /**
     * Searches for cities by name using the geocoding API.
     *
     * @param name The city name or search query
     * @param count Maximum number of results to return (default: 10)
     * @return A [GeocodingResponse] containing matching cities
     */
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
    ): GeocodingResponse
}
