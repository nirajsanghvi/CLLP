package com.vanishingjar.cllp.api.yelp

import com.vanishingjar.cllp.api.yelp.model.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YelpService {
    @GET("v3/businesses/search")
    fun getSearchResults(
        @Query("location") origin: String,
        @Query("term") destination: String): Call<SearchResponse>
}