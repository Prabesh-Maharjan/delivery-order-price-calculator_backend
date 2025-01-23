package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryOrderPriceResponse(
    val total_price: Int,
    val small_order_surcharge: Int,
    val cart_value: Int,
    val delivery: DeliveryDetails
)

@Serializable
data class DeliveryDetails(
    val fee: Int,
    val distance: Int
)

