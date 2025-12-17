package com.example.sportsorganizer.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Singleton object providing Retrofit API service instances.
 *
 * Configures Retrofit with Moshi for JSON serialization and creates
 * lazy-initialized API service instances for weather and geocoding endpoints.
 */
object RetrofitInstance {
    /** Moshi instance configured with Kotlin reflection support for JSON parsing */
    private val moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    /**
     * Lazy-initialized Retrofit service for weather forecast API.
     *
     * Provides access to the Open-Meteo weather forecast endpoints
     * at https://api.open-meteo.com/
     */
    val api: WeatherApiService by lazy {
        Retrofit
            .Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    /**
     * Lazy-initialized Retrofit service for geocoding API.
     *
     * Provides access to the Open-Meteo geocoding endpoints
     * at https://geocoding-api.open-meteo.com/
     */
    val geocodingApi: WeatherApiService by lazy {
        Retrofit
            .Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }
}
