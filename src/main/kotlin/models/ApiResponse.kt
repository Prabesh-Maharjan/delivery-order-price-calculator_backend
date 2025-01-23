package com.example.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiResponse(
    var errorCode: String? = null,
    var statusCode: String? = null,
    var message: String? = null,
    var data: JsonElement? = null
)
