package com.example.utils

import kotlinx.serialization.json.*
import kotlin.math.roundToInt

object DeliveryRangeUtils {
    /**
     * Determines the delivery range for a given distance., if not possible to delivers return null
     *
     * @param distanceInMeters The calculated straight-line distance between users and venue in meters.
     * @param distanceRanges The JSON array containing the distance range returned from dynamic api.
     * @return A Pair of `a` and `b` values if within a range; otherwise, null if delivery is unavailable.
     */
    fun findDeliveryRange(distanceInMeters: Int, distanceRanges: JsonArray): Pair<Double, Double>? {
        val lastRange = distanceRanges.last().jsonObject
        // Ensure the last range has 'max' equal to 0 as per logic
        val maxLastRange = lastRange["max"]?.jsonPrimitive?.int ?: 0
        if (maxLastRange != 0) {
            throw IllegalArgumentException("Last range must have 'max' equal to 0.")
        }
        // Extract the MaxPossibleDistance (min value of the last range)
        val maxPossibleDeliveryDistance = lastRange["min"]?.jsonPrimitive?.int
        if (maxPossibleDeliveryDistance == null || distanceInMeters >= maxPossibleDeliveryDistance) {
            return null
        }
        // Check where calculatedDistance falls within min and max range
        for (range in distanceRanges) {
            val rangeObject = range.jsonObject
            val min = rangeObject["min"]?.jsonPrimitive?.int ?: 0
            val max = rangeObject["max"]?.jsonPrimitive?.int ?: 0


            if (distanceInMeters in min..max) {
                val a = rangeObject["a"]?.jsonPrimitive?.double ?: 0
                val b = rangeObject["b"]?.jsonPrimitive?.double ?: 0
                return Pair(a.toDouble(), b.toDouble())
            }
        }
        return null
    }

    /**
     * Calculates the delivery fee based on the given distance, distance ranges, and base price.
     *
     * @param distanceInMeters The calculated distance between the user and venue.
     * @param distanceRanges The JSON array containing the distance ranges returned from dynamic api.
     * @param basePrice The base price of the delivery service returned from dynamic api.
     * @return The calculated delivery fee,otherwise null if delivery is unavailable.
     */
    fun calculateDeliveryFee(
        distanceInMeters: Int,
        distanceRanges: JsonArray,
        basePrice: Int
    ): Int? {
        // Find the delivery range for the distance
        val deliveryRange = findDeliveryRange(distanceInMeters, distanceRanges)
        // if delivery is not possible then we get null
        if (deliveryRange == null) {
            return null
        }
        val (a, b) = deliveryRange
        val deliveryFee = basePrice + a + (b * distanceInMeters / 10)
        return deliveryFee.roundToInt()
    }

}