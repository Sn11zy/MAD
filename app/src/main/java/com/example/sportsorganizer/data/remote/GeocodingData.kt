package com.example.sportsorganizer.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<City>?,
)

@JsonClass(generateAdapter = true)
data class City(
    @Json(name = "name") val name: String,
    @Json(name = "country") val country: String?,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
)
