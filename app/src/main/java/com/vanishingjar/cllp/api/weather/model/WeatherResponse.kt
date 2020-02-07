package com.vanishingjar.cllp.api.weather.model

data class WeatherResponse (
    val currently : DataPoint?,
    val minutely: DataBlock?,
    val hourly: DataBlock?,
    val daily: DataBlock?,
    val timezone: String
)