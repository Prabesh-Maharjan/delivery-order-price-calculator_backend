package com.example.services

import com.example.config.ApiConfig
import com.example.models.HomeAssignmentStaticResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.net.UnknownHostException


class ApiService(private val client: HttpClient) {

    suspend fun fetchDataFromHomeAssignment(venueSlug: String, isStatic: Boolean): HomeAssignmentStaticResponse {
        val url = if (isStatic) {
            ApiConfig.STATIC_URL.replace("<VENUE_SLUG>", venueSlug)
        } else {
            ApiConfig.DYNAMIC_URL.replace("<VENUE_SLUG>", venueSlug)
        }
        return try {
            val response: HttpResponse = client.get(url)
            // Check if the status code is 200
            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val data = Json.decodeFromString<Map<String, JsonElement>>(responseBody)
                HomeAssignmentStaticResponse(data = data)
            } else {
                throw Exception("Unexpected response status: ${response.status.value}")
            }
        } catch (e: UnknownHostException) {
            throw Exception("Network error: Unable to reach the server.", e)
        } catch (e: Exception) {
            throw Exception("Error fetching venue data: ${e.message}", e)
        }
    }
}
