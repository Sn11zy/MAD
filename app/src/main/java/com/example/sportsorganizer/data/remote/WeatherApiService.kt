package com.example.sportsorganizer.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,precipitation_probability_max,wind_speed_10m_max",
        @Query("timezone") timezone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): WeatherResponse

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
    ): GeocodingResponse
}
