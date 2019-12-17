package com.vanishingjar.cllp.api.github

import com.google.gson.annotations.SerializedName

data class GitHubResponse (
    @SerializedName("html_url") var htmlUrl: String,
    @SerializedName("tag_name") var tag: String,
    var name: String,
    var assets: List<GitHubAsset>
)