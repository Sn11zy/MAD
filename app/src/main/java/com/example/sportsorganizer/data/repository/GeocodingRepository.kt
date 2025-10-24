package com.example.sportsorganizer.data.repository

import com.example.sportsorganizer.data.remote.City
import com.example.sportsorganizer.data.remote.RetrofitInstance
import retrofit2.HttpException
import java.io.IOException

class GeocodingRepository {
    suspend fun searchCity(query: String): Result<List<City>> {
        return try {
            val response = RetrofitInstance.geocodingApi.searchCity(name = query)
            Result.Success(response.results ?: emptyList())
        } catch (e: IOException) {
            Result.Error("No internet connection")
        } catch (e: HttpException) {
            Result.Error("Could not retrieve city data")
        }
    }
}
