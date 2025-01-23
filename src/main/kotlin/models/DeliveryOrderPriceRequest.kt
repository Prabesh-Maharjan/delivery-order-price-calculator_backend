package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryOrderPriceRequest(
    val venueSlug: String,
    val cartValue: Int,
    val userLat: Double,
    val userLon: Double
)
