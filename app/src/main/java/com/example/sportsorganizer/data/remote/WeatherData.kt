package com.example.sportsorganizer.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response model for weather forecast data from Open-Meteo API.
 *
 * Contains daily weather forecasts including temperature, precipitation,
 * and wind speed information.
 *
 * @property daily The daily weather forecast data
 */
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "daily") val daily: DailyWeather,
)

/**
 * Daily weather forecast data.
 *
 * Contains arrays of weather metrics for each day in the forecast period.
 * All arrays have the same length, with indices corresponding to the same day.
 *
 * @property time List of dates in "YYYY-MM-DD" format
 * @property temperatureMax List of maximum temperatures in Celsius for each day
 * @property precipitationProbabilityMax List of maximum precipitation probabilities (0-100%) for each day
 * @property windSpeedMax List of maximum wind speeds in km/h for each day
 */
@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>,
)
