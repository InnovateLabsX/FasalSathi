package com.fasalsaathi.app.ui.faq

import android.graphics.Bitmap

data class ChatMessage(
    val message: String,
    val isFromAi: Boolean,
    val timestamp: String,
    val image: Bitmap? = null
)