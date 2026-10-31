package com.davidbrazuna.droidmentor.data.remote

import com.davidbrazuna.droidmentor.domain.Author
import com.davidbrazuna.droidmentor.domain.ChatMessage

/** Converts a domain message into the Gemini API format. */
fun ChatMessage.toContent(): Content = Content(
    role = when (author) {
        Author.USER -> GeminiRole.USER
        Author.MENTOR -> GeminiRole.MODEL
    },
    parts = listOf(Part(text)),
)
