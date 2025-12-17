package com.example.sportsorganizer.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response model for city search results from Open-Meteo Geocoding API.
 *
 * @property results List of matching cities, or `null` if no matches found
 */
@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<City>?,
)

/**
 * Represents a city with its geographic coordinates.
 *
 * Used for location selection and weather forecast lookups.
 *
 * @property name The name of the city
 * @property country The country code or name (may be null)
 * @property latitude The latitude coordinate
 * @property longitude The longitude coordinate
 */
@JsonClass(generateAdapter = true)
data class City(
    @Json(name = "name") val name: String,
    @Json(name = "country") val country: String?,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
)
