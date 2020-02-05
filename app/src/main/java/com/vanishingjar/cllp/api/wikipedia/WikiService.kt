package com.vanishingjar.cllp.api.wikipedia

import com.vanishingjar.cllp.api.wikipedia.model.WikiSearchResponse
import com.vanishingjar.cllp.api.wikipedia.model.WikiSummaryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikiService {
    @GET("w/api.php?action=query&format=json&list=search&utf8=1")
    fun getSearchResults(
        @Query("srsearch") searchTerm: String): Call<WikiSearchResponse>

    @GET("api/rest_v1/page/summary/{title}")
    fun getSummary(
        @Path("title") title: String): Call<WikiSummaryResponse>
}