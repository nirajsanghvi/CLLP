package com.vanishingjar.cllp.api.weather

import com.vanishingjar.cllp.api.weather.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {
    @GET("forecast/{key}/{lat},{lon}")
    fun getWeatherResults(
        @Path("key") key: String,
        @Path("lat") lat: String,
        @Path("lon") lon: String): Call<WeatherResponse>
}