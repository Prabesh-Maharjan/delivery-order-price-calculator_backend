package com.example.config

object ApiConfig {
    const val BASE_URL = "https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1/venues/"
    const val STATIC_URL = "${BASE_URL}<VENUE_SLUG>/static"
    const val DYNAMIC_URL = "${BASE_URL}<VENUE_SLUG>/dynamic"
}