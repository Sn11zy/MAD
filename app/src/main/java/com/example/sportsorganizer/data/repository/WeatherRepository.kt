package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.remote.RetrofitInstance
import com.example.sportsorganizer.data.remote.WeatherResponse
import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double, date: String): Result<WeatherResponse> {
        return try {
            val response = RetrofitInstance.api.getWeather(
                latitude = latitude,
                longitude = longitude,
                startDate = date,
                endDate = date
            )
            Result.Success(response)
        } catch (e: IOException) {
            Result.Error("No internet connection")
        } catch (e: HttpException) {
            Result.Error("Could not retrieve weather data")
        }
    }
}
