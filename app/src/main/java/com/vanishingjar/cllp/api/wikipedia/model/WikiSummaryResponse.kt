package com.vanishingjar.cllp.api.wikipedia.model

data class WikiSummaryResponse (
    val type: String,
    val displaytitle: String?,
    val extract: String?
)