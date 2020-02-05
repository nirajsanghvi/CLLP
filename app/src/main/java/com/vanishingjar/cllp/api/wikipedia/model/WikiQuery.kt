package com.vanishingjar.cllp.api.wikipedia.model

data class WikiQuery (
    val searchinfo: WikiSearchInfo,
    val search: List<WikiSearchResults> = emptyList()
)