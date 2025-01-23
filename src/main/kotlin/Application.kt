package com.example

import com.example.services.ApiService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val client = HttpClient(CIO)
    val apiService = ApiService(client)

    // Install ContentNegotiation for automatic JSON serialization
    install(ContentNegotiation) {
        json(Json)
    }
    configureRouting(apiService)
}
