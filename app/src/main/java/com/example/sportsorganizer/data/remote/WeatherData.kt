package com.example.sportsorganizer.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "daily") val daily: DailyWeather
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>
)
