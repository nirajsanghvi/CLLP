package com.vanishingjar.cllp.api.github

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("{user}/{repo}/releases/latest")
    fun getLatestRelease(
        @Path("user") user: String,
        @Path("repo") repo: String): Call<GitHubResponse>
}