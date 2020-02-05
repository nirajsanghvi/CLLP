package com.vanishingjar.cllp.api.wikipedia.model

data class WikiSearchResults (
    val title: String,
    val pageid: String?,
    val snippet: String?
)