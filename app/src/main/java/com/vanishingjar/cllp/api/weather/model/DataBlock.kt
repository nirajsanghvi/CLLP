package com.vanishingjar.cllp.api.weather.model

data class DataBlock (
    val summary: String,
    val icon: String,
    val data: List<DataPoint>
)