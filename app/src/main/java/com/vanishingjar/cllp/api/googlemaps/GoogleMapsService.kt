package com.vanishingjar.cllp.api.googlemaps

import com.vanishingjar.cllp.api.googlemaps.model.MapsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
interface GoogleMapsService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "transit"): Call<MapsResponse>
}