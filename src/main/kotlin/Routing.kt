package com.example

import com.example.models.ApiResponse
import com.example.models.DeliveryDetails
import com.example.models.DeliveryOrderPriceRequest
import com.example.models.DeliveryOrderPriceResponse
import com.example.services.ApiService
import com.example.utils.CentralUtils
import com.example.utils.DeliveryRangeUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

fun Application.configureRouting(apiService: ApiService) {
    routing {
        route("/api/v1") {
            get("/delivery-order-price") {
                val venueSlug = call.request.queryParameters["venue_slug"]
                val cartValue = call.request.queryParameters["cart_value"]?.toIntOrNull()
                val userLat = call.request.queryParameters["user_lat"]?.toDoubleOrNull()
                val userLon = call.request.queryParameters["user_lon"]?.toDoubleOrNull()

                val missingParams = mutableListOf<String>()
                if (venueSlug.isNullOrBlank()) missingParams.add("venue_slug")
                if (cartValue == null || cartValue <= 0) missingParams.add("cart_value")
                if (userLat == null) missingParams.add("user_lat")
                if (userLon == null) missingParams.add("user_lon")
                // If there are any missing or invalid parameters
                if (missingParams.isNotEmpty()) {
                    val errorMessage =
                        "The following query parameters must be valid: ${missingParams.joinToString(", ")}"
                    call.respond(
                        ApiResponse(
                            errorCode = "1",
                            statusCode = HttpStatusCode.BadRequest.value.toString(),
                            message = errorMessage,
                            data = null
                        )
                    )
                }

                val request = DeliveryOrderPriceRequest(
                    venueSlug = venueSlug ?: "",
                    cartValue = cartValue ?: 0,
                    userLat = userLat ?: 0.0,
                    userLon = userLon ?: 0.0
                )
                try {
                    // Fetch static data for venue
                    val staticData = withContext(Dispatchers.IO) {
                        apiService.fetchDataFromHomeAssignment(
                            request.venueSlug,
                            isStatic = true
                        )
                    }
                    val venueRawStatic = staticData.data["venue_raw"] as? JsonObject
                    val location = venueRawStatic?.get("location")?.jsonObject
                    val coordinates = location?.get("coordinates")?.jsonArray
                    // Check if coordinates are available
                    if (coordinates.isNullOrEmpty()) {
                        call.respond(
                            ApiResponse(
                                errorCode = "1",
                                statusCode = HttpStatusCode.BadRequest.value.toString(),
                                message = "Venue location coordinates are missing or invalid",
                                data = null
                            )
                        )
                        return@get
                    }
                    val venueLat = coordinates.get(0).jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                    val venueLon = coordinates.get(1).jsonPrimitive.content.toDoubleOrNull() ?: 0.0

                    // Calculate straight-line distance
                    val actualDistanceInMeters =
                        CentralUtils.calculateHaversineDistance(request.userLat, request.userLon, venueLat, venueLon)

                    // Fetch dynamic data for venue (order minimum, delivery pricing)
                    val dynamicData = withContext(Dispatchers.IO) {
                        apiService.fetchDataFromHomeAssignment(
                            request.venueSlug,
                            isStatic = false
                        )
                    }
                    val venueRawDynamic = dynamicData.data["venue_raw"] as? JsonObject
                    val deliverySpecs = venueRawDynamic?.get("delivery_specs")?.jsonObject
                    val orderMinimumNoSurcharge =
                        deliverySpecs?.get("order_minimum_no_surcharge")?.jsonPrimitive?.content?.toIntOrNull()
                    val deliveryPricing = deliverySpecs?.get("delivery_pricing")?.jsonObject
                    val basePrice = deliveryPricing?.get("base_price")?.jsonPrimitive?.content?.toIntOrNull()
                    val distanceRanges = deliveryPricing?.get("distance_ranges")?.jsonArray

                    if (orderMinimumNoSurcharge == null || basePrice == null || distanceRanges == null) {
                        call.respond(
                            ApiResponse(
                                errorCode = "1",
                                statusCode = HttpStatusCode.BadRequest.value.toString(),
                                message = "Missing some data like order_minimum_no_surcharge, base_price, or distance_ranges",
                                data = null
                            )
                        )
                        return@get
                    }

                    val deliveryFee =
                        DeliveryRangeUtils.calculateDeliveryFee(actualDistanceInMeters, distanceRanges, basePrice)
                    if (deliveryFee == null) {
                        call.respond(
                            ApiResponse(
                                errorCode = "1",
                                statusCode = "400",
                                message = "Delivery in this location is not possible[Estimated distance:${actualDistanceInMeters} which is too far]",
                                data = null
                            )
                        )
                        return@get

                    }

                    val smallOrderSurcharge = (orderMinimumNoSurcharge - request.cartValue).coerceAtLeast(0)
                    val totalPrice = request.cartValue + smallOrderSurcharge + deliveryFee

                    // Create the response using the DeliveryOrderPriceResponse class
                    val response = DeliveryOrderPriceResponse(
                        total_price = totalPrice,
                        small_order_surcharge = smallOrderSurcharge,
                        cart_value = request.cartValue,
                        delivery = DeliveryDetails(fee = deliveryFee, distance = actualDistanceInMeters)
                    )
                    call.respond(
                        ApiResponse(
                            errorCode = "0",
                            statusCode = HttpStatusCode.OK.value.toString(),
                            message = "Success",
                            data = Json.encodeToJsonElement(response)
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        ApiResponse(
                            errorCode = "500",
                            statusCode = HttpStatusCode.InternalServerError.value.toString(),
                            message = "An error occurred: ${e.message}",
                            data = null
                        )
                    )
                    return@get
                }
            }
        }
    }
}

