package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.remote.RetrofitInstance
import com.example.sportsorganizer.data.remote.WeatherResponse
import retrofit2.HttpException
import java.io.IOException

/**
 * Sealed class representing the result of a network operation.
 *
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data.
     *
     * @property data The successfully retrieved data
     */
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    /**
     * Represents a failed operation with an error message.
     *
     * @property message A human-readable error message
     */
    data class Error(
        val message: String,
    ) : Result<Nothing>()
}

/**
 * Repository for fetching weather forecast data from the Open-Meteo API.
 *
 * Provides weather information for specific coordinates and dates,
 * handling network errors and returning results in a sealed [Result] type.
 */
class WeatherRepository {
    /**
     * Fetches weather forecast data for a specific location and date.
     *
     * @param latitude The latitude coordinate of the location
     * @param longitude The longitude coordinate of the location
     * @param date The date for which to fetch weather data (format: "YYYY-MM-DD")
     * @return A [Result] containing either the [WeatherResponse] or an error message
     *
     * @sample
     * ```
     * val result = weatherRepository.getWeather(51.5074, -0.1278, "2024-12-25")
     * when (result) {
     *     is Result.Success -> println("Temp: ${result.data.daily.temperatureMax[0]}°C")
     *     is Result.Error -> println("Error: ${result.message}")
     * }
     * ```
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        date: String,
    ): Result<WeatherResponse> =
        try {
            val response =
                RetrofitInstance.api.getWeather(
                    latitude = latitude,
                    longitude = longitude,
                    startDate = date,
                    endDate = date,
                )
            Result.Success(response)
        } catch (e: IOException) {
            Result.Error("No internet connection")
        } catch (e: HttpException) {
            Result.Error("Could not retrieve weather data")
        }
}
