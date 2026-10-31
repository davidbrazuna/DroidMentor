package com.davidbrazuna.droidmentor

import android.app.Application
import androidx.room.Room
import com.davidbrazuna.droidmentor.data.local.AppDatabase
import com.davidbrazuna.droidmentor.data.remote.GeminiService
import com.davidbrazuna.droidmentor.data.remote.HttpClientFactory
import io.ktor.client.HttpClient

class DroidMentorApp : Application() {

    // Created once, on first use, and shared by the whole app
    val httpClient: HttpClient by lazy {
        HttpClientFactory.create(enableLogging = BuildConfig.DEBUG)
    }

    val geminiService: GeminiService by lazy {
        GeminiService(
            client = httpClient,
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY },   // temporary, until Settings (BYOK)
        )
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "droidmentor.db").build()
    }
}