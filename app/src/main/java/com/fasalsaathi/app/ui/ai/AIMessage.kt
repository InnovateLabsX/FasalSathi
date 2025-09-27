package com.fasalsaathi.app.ui.ai

data class AIMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val isSystemMessage: Boolean = false
)