package com.vanishingjar.cllp.api.github

import com.google.gson.annotations.SerializedName

data class GitHubAsset (
    var url: String,
    @SerializedName("browser_download_url") var downloadUrl: String
)