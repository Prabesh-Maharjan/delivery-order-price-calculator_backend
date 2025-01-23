package com.example

import com.example.models.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals


class ApplicationTest {

    @Test
    fun testValidResult() = testApplication {
        application {
            module()
        }
        val cartValueSend = 50
        //  Call endpoint with valid parameters
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("cart_value", cartValueSend)
            parameter("user_lat", "24.92813512")
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("0", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("Success") == true)
        val cartValue = apiResponse.data?.jsonObject?.get("cart_value")?.jsonPrimitive?.int
        assertEquals(cartValueSend, cartValue)// should always be same as request value
    }


    @Test
    fun testWhenVenueSlugIsNotSendResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint without venue_slug parameters which is mandatory
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("cart_value", 50)
            parameter("user_lat", "24.92813512")
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("venue_slug") == true)
    }

    @Test
    fun testWhenCartValueIsNotSendResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint without cart_value parameters which is mandatory
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("user_lat", "24.92813512")
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("cart_value") == true)
    }

    @Test
    fun testWhenUserLatIsNotSendResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint without user_lat parameters which is mandatory
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("cart_value", 50)
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("user_lat") == true)
    }


    @Test
    fun testWhenUserLonIsNotSendResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint without user_lon parameters which is mandatory
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("user_lat", "24.92813512")
            parameter("cart_value", 50)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("user_lon") == true)
    }


    @Test
    fun testExceedDistanceLimitResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint with user coordinate of berlin to venue in helsinki so long distance
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("cart_value", 50)
            parameter("user_lat", "13.4536149")
            parameter("user_lon", "52.5003197")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("Delivery in this location is not possible[Estimated distance") == true)
    }

    @Test
    fun testWhenInvalidVenueSlugResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint with invalid venue_slug like 'wrongValueTest'
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "wrongValueTest")
            parameter("cart_value", 50)
            parameter("user_lat", "24.92813512")
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("500", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("error occurred") == true)
    }

    @Test
    fun testWhenCartValueIsNotPositiveNumberResult() = testApplication {
        application {
            module()
        }
        //  Call endpoint with invalid cart_value like negative value
        val response = client.get("/api/v1/delivery-order-price") {
            parameter("venue_slug", "home-assignment-venue-helsinki")
            parameter("cart_value", -1)
            parameter("user_lat", "24.92813512")
            parameter("user_lon", "60.17012143")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<String>()
        val apiResponse = Json.decodeFromString<ApiResponse>(responseBody)
        assertEquals("1", apiResponse.errorCode)
        assertTrue(apiResponse.message?.contains("cart_value") == true)
    }


}
