package com.davidbrazuna.droidmentor.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(enableLogging: Boolean): HttpClient = HttpClient(OkHttp) {

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 60_000
        }

        if (enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("DroidMentorHttp", message)
                    }
                }
                level = LogLevel.BODY
                sanitizeHeader { header -> header == "x-goog-api-key" }
            }
        }

        defaultRequest {
            url("https://generativelanguage.googleapis.com/v1beta/")
            contentType(ContentType.Application.Json)
        }
    }
}