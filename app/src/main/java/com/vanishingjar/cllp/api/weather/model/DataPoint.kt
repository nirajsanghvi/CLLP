package com.vanishingjar.cllp.api.weather.model

data class DataPoint (
    val time: Long,
    val summary: String?,
    val temperature: Double?,
    val apparentTemperature: Double?,
    val temperatureHigh: Double?,
    val temperatureLow: Double?,
    val windSpeed: Double?,
    val icon: String?
) {
    val weatherIcon: String
        get() {
            return when(icon) {
                "clear-day" -> "[Clear]"
                "clear-night" -> "[Clear]"
                "rain" -> "[Rainy]"
                "snow" -> "[Snowy]"
                "sleet" -> "[Sleet]"
                "wind" -> "[Windy]"
                "fog" -> "[Foggy]"
                "cloudy" -> "[Cloudy]"
                "partly-cloudy-day" -> "[P. Cloudy]"
                "partly-cloudy-night" -> "[P. Cloudy]"
                "hail" -> "[Hail]"
                "thunderstorm" -> "[Thunderstorms]"
                "tornado" -> "[Tornado]"
                else -> ""
            }
        }
}