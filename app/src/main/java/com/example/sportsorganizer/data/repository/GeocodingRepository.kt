package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.remote.City
import com.example.sportsorganizer.data.remote.RetrofitInstance
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for searching cities using the Open-Meteo Geocoding API.
 *
 * Provides location search functionality to find cities and their coordinates,
 * which can be used for weather forecast lookups.
 */
class GeocodingRepository {
    /**
     * Searches for cities matching the given query string.
     *
     * Returns a list of matching cities with their names, countries, and coordinates.
     * The API returns up to 10 results by default.
     *
     * @param query The city name or search term
     * @return A [Result] containing either a list of [City] objects or an error message
     *
     * @sample
     * ```
     * val result = geocodingRepository.searchCity("London")
     * when (result) {
     *     is Result.Success -> result.data.forEach { city ->
     *         println("${city.name}, ${city.country}: (${city.latitude}, ${city.longitude})")
     *     }
     *     is Result.Error -> println("Error: ${result.message}")
     * }
     * ```
     */
    suspend fun searchCity(query: String): Result<List<City>> =
        try {
            val response = RetrofitInstance.geocodingApi.searchCity(name = query)
            Result.Success(response.results ?: emptyList())
        } catch (e: IOException) {
            Result.Error("No internet connection")
        } catch (e: HttpException) {
            Result.Error("Could not retrieve city data")
        }
}
